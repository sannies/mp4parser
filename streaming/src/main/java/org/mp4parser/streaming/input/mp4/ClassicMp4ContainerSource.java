package org.mp4parser.streaming.input.mp4;

import org.mp4parser.BasicContainer;
import org.mp4parser.Box;
import org.mp4parser.BoxParser;
import org.mp4parser.PropertyBoxParserImpl;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.streaming.StreamingSample;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.TrackExtension;
import org.mp4parser.streaming.extensions.CompositionTimeSampleExtension;
import org.mp4parser.streaming.extensions.CompositionTimeTrackExtension;
import org.mp4parser.streaming.extensions.SampleFlagsSampleExtension;
import org.mp4parser.streaming.extensions.TrackIdTrackExtension;
import org.mp4parser.streaming.input.StreamingSampleImpl;
import org.mp4parser.streaming.output.SampleSink;
import org.mp4parser.streaming.output.mp4.FragmentedMp4Writer;
import org.mp4parser.tools.Path;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.concurrent.Callable;

import static org.mp4parser.tools.CastUtils.l2i;

/**
 * Creates a List of StreamingTrack from a classic MP4. Fragmented MP4s don't
 * work and the implementation will consume a lot of heap when the MP4
 * is not a 'fast-start' MP4 (order: ftyp, moov, mdat good;
 * order ftyp, mdat, moov bad).
 */
// @todo implement FragmentedMp4ContainerSource
// @todo store mdat of non-fast-start MP4 on disk
public class ClassicMp4ContainerSource implements Callable<Void> {
    final HashMap<TrackBox, Mp4StreamingTrack> tracks = new LinkedHashMap<TrackBox, Mp4StreamingTrack>();
    final HashMap<TrackBox, Long> currentChunks = new HashMap<TrackBox, Long>();
    final HashMap<TrackBox, Long> currentSamples = new HashMap<TrackBox, Long>();
    final DiscardingByteArrayOutputStream baos = new DiscardingByteArrayOutputStream();
    final ReadableByteChannel readableByteChannel;
    private final ByteBuffer BUFFER = ByteBuffer.allocateDirect(65535);


    public ClassicMp4ContainerSource(InputStream is) throws IOException {
        readableByteChannel = Channels.newChannel(new TeeInputStream(is, baos));
        BasicContainer container = new BasicContainer();
        BoxParser boxParser = new PropertyBoxParserImpl();
        Box current = null;

        while (current == null || !"moov".equals(current.getType())) {
            current = boxParser.parseBox(readableByteChannel, null);
            container.addBox(current);
        }
        // Either mdat was already read (yeahh sucks but what can you do if it's in the beginning)
        // or it's still coming

        for (TrackBox trackBox : Path.<TrackBox>getPaths(container, "moov[0]/trak")) {
            Mp4StreamingTrack mp4StreamingTrack = new Mp4StreamingTrack(trackBox);
            tracks.put(trackBox, mp4StreamingTrack);
            if (trackBox.getSampleTableBox().getCompositionTimeToSample() != null) {
                mp4StreamingTrack.addTrackExtension(new CompositionTimeTrackExtension());
            }
            mp4StreamingTrack.addTrackExtension(new TrackIdTrackExtension(trackBox.getTrackHeaderBox().getTrackId()));
            currentChunks.put(trackBox, 1L);
            currentSamples.put(trackBox, 1L);
        }
    }

    public static void main(String[] args) throws IOException {
        ClassicMp4ContainerSource classicMp4ContainerSource = null;
        try {
            classicMp4ContainerSource = new ClassicMp4ContainerSource(new URI("http://org.mp4parser.s3.amazonaws.com/examples/Cosmos%20Laundromat%20small%20faststart.mp4").toURL().openStream());
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
        List<StreamingTrack> streamingTracks = classicMp4ContainerSource.getTracks();
        File f = new File("output.mp4");
        FragmentedMp4Writer writer = new FragmentedMp4Writer(streamingTracks, new FileOutputStream(f).getChannel());

        System.out.println("Reading and writing started.");
        classicMp4ContainerSource.call();
        writer.close();
        System.err.println(f.getAbsolutePath());

    }

    List<StreamingTrack> getTracks() {
        return new ArrayList<StreamingTrack>(tracks.values());
    }

    public Void call() throws IOException {


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

                // Sample Flags Start
                SampleDependencyTypeBox sdtp = Path.getPath(stbl, "sdtp");
                SampleFlagsSampleExtension sfse = new SampleFlagsSampleExtension();
                if (sdtp != null) {
                    SampleDependencyTypeBox.Entry e = sdtp.getEntries().get(l2i(index));
                    sfse.setIsLeading(e.getIsLeading());
                    sfse.setSampleDependsOn(e.getSampleDependsOn());
                    sfse.setSampleIsDependedOn(e.getSampleIsDependedOn());
                    sfse.setSampleHasRedundancy(e.getSampleHasRedundancy());
                }
                if (stbl.getSyncSampleBox() != null) {
                    if (Arrays.binarySearch(stbl.getSyncSampleBox().getSampleNumber(), index) >= 0) {
                        sfse.setSampleIsNonSyncSample(false);
                    } else {
                        sfse.setSampleIsNonSyncSample(true);
                    }
                }

                DegradationPriorityBox stdp = Path.getPath(stbl, "stdp");
                if (stdp != null) {
                    sfse.setSampleDegradationPriority(stdp.getPriorities()[l2i(index)]);
                }
                // Sample Flags Done

                int sampleSize = l2i(stsz.getSampleSizeAtIndex(l2i(index - 1)));
                long avail = baos.available();

                // as long as the sample has not yet been fully read
                // read more bytes from the input channel to fill
                //
                while (avail <= offset + sampleSize) {
                    try {
                        int br = readableByteChannel.read(BUFFER);
                        if (br == -1) {
                            break;
                        }
                        avail = baos.available();
                        ((Buffer)BUFFER).rewind();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
                //System.err.println("Get sample content @" + offset + " len=" + sampleSize);
                final byte[] sampleContent = baos.get(offset, sampleSize);

                StreamingSample streamingSample = new StreamingSampleImpl(sampleContent, duration);
                streamingSample.addSampleExtension(sfse);
                if (compositionOffsets != null && !compositionOffsets.isEmpty()) {
                    final long compositionOffset = compositionOffsets.get(0).getOffset();
                    if (compositionOffsets.get(0).getCount() == 1) {
                        compositionOffsets.remove(0);
                    } else {
                        compositionOffsets.get(0).setCount(compositionOffsets.get(0).getCount() - 1);
                    }
                    streamingSample.addSampleExtension(CompositionTimeSampleExtension.create(compositionOffset));
                }

                if (firstInLine.getTrackHeaderBox().getTrackId() == 1) {
                    System.out.println("Pushing sample @" + offset + " of " + sampleSize + " bytes (i=" + index + ")");
                }
                tracks.get(firstInLine).getSampleSink().acceptSample(streamingSample, tracks.get(firstInLine));


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


        return null;
    }

    public static class Mp4StreamingTrack implements StreamingTrack {

        private final TrackBox trackBox;
        protected HashMap<Class<? extends TrackExtension>, TrackExtension> trackExtensions = new HashMap<Class<? extends TrackExtension>, TrackExtension>();
        boolean allSamplesRead = false;
        SampleSink sampleSink;

        public Mp4StreamingTrack(TrackBox trackBox) {
            this.trackBox = trackBox;
        }

        public void close() {
            allSamplesRead = true;
        }

        public boolean isClosed() {
            return allSamplesRead;
        }

        public long getTimescale() {
            return trackBox.getMediaBox().getMediaHeaderBox().getTimescale();
        }

        public SampleSink getSampleSink() {
            return sampleSink;
        }

        public void setSampleSink(SampleSink sampleSink) {
            this.sampleSink = sampleSink;
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

        /**
         * The output stream that will receive a copy of all bytes read from the
         * proxied input stream.
         */
        private final OutputStream branch;
        long counter = 0;


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