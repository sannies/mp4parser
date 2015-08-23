package com.mp4parser.streaming.containersource;

import com.mp4parser.BasicContainer;
import com.mp4parser.Box;
import com.mp4parser.BoxParser;
import com.mp4parser.PropertyBoxParserImpl;
import com.mp4parser.boxes.iso14496.part12.CompositionTimeToSample;
import com.mp4parser.boxes.iso14496.part12.DegradationPriorityBox;
import com.mp4parser.boxes.iso14496.part12.SampleDependencyTypeBox;
import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import com.mp4parser.boxes.iso14496.part12.SampleSizeBox;
import com.mp4parser.boxes.iso14496.part12.SampleTableBox;
import com.mp4parser.boxes.iso14496.part12.SampleToChunkBox;
import com.mp4parser.boxes.iso14496.part12.TimeToSampleBox;
import com.mp4parser.boxes.iso14496.part12.TrackBox;
import com.mp4parser.boxes.iso14496.part12.TrackHeaderBox;
import com.mp4parser.streaming.*;
import com.mp4parser.streaming.extensions.CompositionTimeSampleExtension;
import com.mp4parser.streaming.extensions.CompositionTimeTrackExtension;
import com.mp4parser.streaming.extensions.SampleFlagsSampleExtension;
import com.mp4parser.streaming.extensions.TrackIdTrackExtension;
import com.mp4parser.tools.Path;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.mp4parser.tools.CastUtils.l2i;

public class Mp4ContainerSource {
    private final ByteBuffer BUFFER = ByteBuffer.allocateDirect(65535);

    static class ConsumeSamplesCallable implements Callable {

        private StreamingTrack streamingTrack;

        public ConsumeSamplesCallable(StreamingTrack streamingTrack) {
            this.streamingTrack = streamingTrack;
        }

        public Object call() throws Exception {
            do {
                try {
                    StreamingSample ss;
                    while ((ss = streamingTrack.getSamples().poll(100, TimeUnit.MILLISECONDS)) != null) {
                        // consumeSample(streamingTrack, ss);
                        System.out.println(streamingTrack.getTrackExtension(TrackIdTrackExtension.class).getTrackId() + ": " + ss.getDuration());
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (streamingTrack.hasMoreSamples());
            return null;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Mp4ContainerSource mp4ContainerSource = new Mp4ContainerSource();
        List<StreamingTrack> streamingTracks = mp4ContainerSource.doParse(new FileInputStream("C:\\content\\Surfing_RedBull.mp4_smooth_246x144_138.h264.mp4"));

        MultiTrackFragmentedMp4Writer writer = new MultiTrackFragmentedMp4Writer(streamingTracks, new FileOutputStream("output.mp4"));
        writer.write();


        //ExecutorService es = Executors.newFixedThreadPool(streamingTracks.size());
        //for (StreamingTrack streamingTrack : streamingTracks) {
        //    es.submit(new ConsumeSamplesCallable(streamingTrack));
        //}
        // es.shutdown();


    }

    List<StreamingTrack> doParse(InputStream is) throws IOException {
        final DiscardingByteArrayOutputStream baos = new DiscardingByteArrayOutputStream();
        final ReadableByteChannel readableByteChannel = Channels.newChannel(new TeeInputStream(is, baos));
        BasicContainer container = new BasicContainer();
        BoxParser boxParser = new PropertyBoxParserImpl();
        Box current = null;

        while (current == null || !"moov".equals(current.getType())) {
            current = boxParser.parseBox(readableByteChannel, null);
            container.addBox(current);
        }
        // Either mdat was already read (yeahh sucks but what can you do if it's in the beginning)
        // or it's still coming
        final HashMap<TrackBox, Mp4StreamingTrack> tracks = new HashMap<TrackBox, Mp4StreamingTrack>();
        final HashMap<TrackBox, Long> currentChunks = new HashMap<TrackBox, Long>();
        final HashMap<TrackBox, Long> currentSamples = new HashMap<TrackBox, Long>();

        for (TrackBox trackBox : Path.<TrackBox>getPaths(container, "moov/trak")) {
            Mp4StreamingTrack mp4StreamingTrack = new Mp4StreamingTrack(trackBox);
            tracks.put(trackBox, mp4StreamingTrack);
            if (trackBox.getSampleTableBox().getCompositionTimeToSample() != null) {
                mp4StreamingTrack.addTrackExtension(new CompositionTimeTrackExtension());
            }
            mp4StreamingTrack.addTrackExtension(new TrackIdTrackExtension(trackBox.getTrackHeaderBox().getTrackId()));
            currentChunks.put(trackBox, 1L);
            currentSamples.put(trackBox, 1L);
        }

        new Thread(new Runnable() {
            public void run() {
                while (true) {
                    TrackBox firstInLine = null;

                    long currentChunk = 0;
                    long currentChunkStartSample = 0;
                    long offset = Long.MAX_VALUE;
                    SampleToChunkBox.Entry entry = null;
                    for (TrackBox trackBox : tracks.keySet()) {
                        long _currentChunk = currentChunks.get(trackBox);
                        long _currentSample = currentSamples.get(trackBox);
                        long[] chunkOffsets = trackBox.getSampleTableBox().getChunkOffsetBox().getChunkOffsets();

                        if ((l2i(_currentChunk) - 1 < chunkOffsets.length) && chunkOffsets[l2i(_currentChunk) - 1] < offset) {

                            firstInLine = trackBox;
                            currentChunk = _currentChunk;
                            currentChunkStartSample = _currentSample;
                            offset = chunkOffsets[l2i(_currentChunk) - 1];
                        }
                    }
                    if (firstInLine == null) {
                        break;
                    }

                    SampleToChunkBox stsc = firstInLine.getSampleTableBox().getSampleToChunkBox();
                    for (SampleToChunkBox.Entry _entry : stsc.getEntries()) {
                        if (currentChunk >= _entry.getFirstChunk()) {
                            entry = _entry;
                        } else {
                            break;
                        }
                    }

                    long trackId = firstInLine.getTrackHeaderBox().getTrackId();
                    assert entry != null;
                    SampleTableBox stbl = firstInLine.getSampleTableBox();

                    List<TimeToSampleBox.Entry> times = stbl.getTimeToSampleBox().getEntries();
                    List<CompositionTimeToSample.Entry> compositionOffsets = stbl.getCompositionTimeToSample() != null ? stbl.getCompositionTimeToSample().getEntries() : null;

                    //System.out.println(trackId + ": Pushing chunk with sample " + currentChunkStartSample + "(offset: " + offset + ") to " + (currentChunkStartSample + entry.getSamplesPerChunk()) + " in the chunk");
                    SampleSizeBox stsz = stbl.getSampleSizeBox();


                    for (long index = currentChunkStartSample; index < currentChunkStartSample + entry.getSamplesPerChunk(); index++) {
                        final long duration = times.get(0).getDelta();
                        if (times.get(0).getCount() == 1) {
                            times.remove(0);
                        } else {
                            times.get(0).setCount(times.get(0).getCount() - 1);
                        }
                        final ArrayList<SampleExtension> extensions = new ArrayList<SampleExtension>();
                        if (compositionOffsets != null) {
                            final long compositionOffset = compositionOffsets.get(0).getOffset();
                            if (compositionOffsets.get(0).getCount() == 1) {
                                compositionOffsets.remove(0);
                            } else {
                                compositionOffsets.get(0).setCount(compositionOffsets.get(0).getCount() - 1);
                            }
                            extensions.add(CompositionTimeSampleExtension.create(compositionOffset));
                        }
                        SampleDependencyTypeBox sdtp = Path.getPath(stbl, "sdtp");
                        SampleFlagsSampleExtension sfse = null;
                        if (sdtp != null) {
                            SampleDependencyTypeBox.Entry e = sdtp.getEntries().get(l2i(index));
                            if (sfse == null) {
                                sfse = new SampleFlagsSampleExtension();
                            }

                            sfse.setIsLeading(e.getIsLeading());
                            sfse.setSampleDependsOn(e.getSampleDependsOn());
                            sfse.setSampleIsDependedOn(e.getSampleIsDependedOn());
                            sfse.setSampleHasRedundancy(e.getSampleHasRedundancy());
                        }
                        if (stbl.getSyncSampleBox() != null) {
                            if (sfse == null) {
                                sfse = new SampleFlagsSampleExtension();
                            }
                            if (Arrays.binarySearch(stbl.getSyncSampleBox().getSampleNumber(), index) >= 0) {
                                sfse.setSampleIsNonSyncSample(false);
                            } else {
                                sfse.setSampleIsNonSyncSample(true);
                            }
                        }

                        DegradationPriorityBox stdp = Path.getPath(stbl, "stdp");
                        if (stdp != null) {
                            if (sfse == null) {
                                sfse = new SampleFlagsSampleExtension();
                            }
                            sfse.setSampleDegradationPriority(stdp.getPriorities()[l2i(index)]);
                        }

                        int sampleSize = l2i(stsz.getSampleSizeAtIndex(l2i(index - 1)));
                        long avail = baos.available();
                        int bytesRead = 0;

                        while (avail + bytesRead <= offset + sampleSize) {
                            try {
                                bytesRead += readableByteChannel.read(BUFFER);
                                BUFFER.rewind();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        final byte[] sampleContent = baos.get(offset, sampleSize);

                        StreamingSample ss = new StreamingSampleImpl(Collections.singletonList(sampleContent), duration);

                        try {
                            //System.out.print("Pushing sample @" + offset + " of " + sampleSize + " bytes (i=" + index + ")");
                            tracks.get(firstInLine).getSamples().put(ss);
                            //System.out.println("Pushed");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        offset += sampleSize;


                    }
                    baos.discardTo(offset);
                    currentChunks.put(firstInLine, currentChunk + 1);
                    currentSamples.put(firstInLine, currentChunkStartSample + entry.getSamplesPerChunk());

                }
                for (Mp4StreamingTrack mp4StreamingTrack : tracks.values()) {
                    mp4StreamingTrack.close();
                }
                System.out.println("All Samples read.");
            }
        }).start();
        return new ArrayList<StreamingTrack>(tracks.values());

    }

    public static class Mp4StreamingTrack implements StreamingTrack {

        private final TrackBox trackBox;
        private BlockingQueue<StreamingSample> samples = new ArrayBlockingQueue<StreamingSample>(1000);
        protected HashMap<Class<? extends TrackExtension>, TrackExtension> trackExtensions = new HashMap<Class<? extends TrackExtension>, TrackExtension>();
        boolean allSamplesRead = false;

        public void close() {
            allSamplesRead = true;
        }



        public Mp4StreamingTrack(TrackBox trackBox) {
            this.trackBox = trackBox;
        }


        public long getTimescale() {
            return trackBox.getMediaBox().getMediaHeaderBox().getTimescale();
        }

        public BlockingQueue<StreamingSample> getSamples() {
            return samples;
        }

        public boolean hasMoreSamples() {
            return !allSamplesRead && !samples.isEmpty();
        }

        public TrackHeaderBox getTrackHeaderBox() {
            return trackBox.getTrackHeaderBox();
        }

        public String getHandler() {
            return trackBox.getMediaBox().getHandlerBox().getHandlerType();
        }

        public String getLanguage() {
            return trackBox.getMediaBox().getMediaHeaderBox().getLanguage();
        }

        public SampleDescriptionBox getSampleDescriptionBox() {
            return trackBox.getSampleTableBox().getSampleDescriptionBox();
        }

        public <T extends TrackExtension> T getTrackExtension(Class<T> clazz) {
            return (T) trackExtensions.get(clazz);
        }

        public void addTrackExtension(TrackExtension trackExtension) {

            trackExtensions.put(trackExtension.getClass(), trackExtension);
        }

        public void removeTrackExtension(Class<? extends TrackExtension> clazz) {
            trackExtensions.remove(clazz);
        }
    }

    public static class TeeInputStream extends FilterInputStream {

        long counter = 0;

        /**
         * The output stream that will receive a copy of all bytes read from the
         * proxied input stream.
         */
        private final OutputStream branch;


        /**
         * Creates a TeeInputStream that proxies the given {@link InputStream}
         * and copies all read bytes to the given {@link OutputStream}. The given
         * output stream will not be closed when this stream gets closed.
         *
         * @param input  input stream to be proxied
         * @param branch output stream that will receive a copy of all bytes read
         */
        public TeeInputStream(InputStream input, OutputStream branch) {

            super(input);
            this.branch = branch;

        }

        /**
         * Reads a single byte from the proxied input stream and writes it to
         * the associated output stream.
         *
         * @return next byte from the stream, or -1 if the stream has ended
         * @throws IOException if the stream could not be read (or written)
         */
        @Override
        public int read() throws IOException {
            int ch = super.read();
            if (ch != -1) {
                branch.write(ch);
                counter++;
            }
            return ch;
        }

        /**
         * Reads bytes from the proxied input stream and writes the read bytes
         * to the associated output stream.
         *
         * @param bts byte buffer
         * @param st  start offset within the buffer
         * @param end maximum number of bytes to read
         * @return number of bytes read, or -1 if the stream has ended
         * @throws IOException if the stream could not be read (or written)
         */
        @Override
        public int read(byte[] bts, int st, int end) throws IOException {
            int n = super.read(bts, st, end);
            if (n != -1) {
                branch.write(bts, st, n);
                counter += n;
            }
            return n;
        }

        /**
         * Reads bytes from the proxied input stream and writes the read bytes
         * to the associated output stream.
         *
         * @param bts byte buffer
         * @return number of bytes read, or -1 if the stream has ended
         * @throws IOException if the stream could not be read (or written)
         */
        @Override
        public int read(byte[] bts) throws IOException {
            int n = super.read(bts);
            if (n != -1) {
                branch.write(bts, 0, n);
                counter += n;
            }
            return n;
        }

    }

}