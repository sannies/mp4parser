package org.mp4parser.streaming.output.mp4;

import org.mp4parser.Box;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.streaming.StreamingSample;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.extensions.SampleFlagsSampleExtension;
import org.mp4parser.streaming.output.SampleSink;
import org.mp4parser.tools.Mp4Arrays;
import org.mp4parser.tools.Path;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates an MP4
 */
public class StandardMp4Writer extends DefaultBoxes implements SampleSink {

    public static final Object OBJ = new Object();
    private static final Logger LOG = Logger.getLogger(FragmentedMp4Writer.class.getName());
    protected final WritableByteChannel sink;
    protected List<StreamingTrack> source;
    protected Date creationTime;
    protected long sequenceNumber = 1;
    protected Map<StreamingTrack, CountDownLatch> congestionControl = new ConcurrentHashMap<StreamingTrack, CountDownLatch>();
    /**
     * Contains the start time of the next segment in line that will be created.
     */
    protected Map<StreamingTrack, Long> nextChunkCreateStartTime = new ConcurrentHashMap<StreamingTrack, Long>();
    /**
     * Contains the start time of the next segment in line that will be written.
     */
    protected Map<StreamingTrack, Long> nextChunkWriteStartTime = new ConcurrentHashMap<StreamingTrack, Long>();
    /**
     * Contains the next sample's start time.
     */
    protected Map<StreamingTrack, Long> nextSampleStartTime = new HashMap<StreamingTrack, Long>();
    /**
     * Buffers the samples per track until there are enough samples to form a Segment.
     */
    protected Map<StreamingTrack, List<StreamingSample>> sampleBuffers = new HashMap<StreamingTrack, List<StreamingSample>>();
    protected Map<StreamingTrack, TrackBox> trackBoxes = new HashMap<StreamingTrack, TrackBox>();
    /**
     * Buffers segements until it's time for a segment to be written.
     */
    protected Map<StreamingTrack, Queue<ChunkContainer>> chunkBuffers = new ConcurrentHashMap<StreamingTrack, Queue<ChunkContainer>>();
    protected Map<StreamingTrack, Long> chunkNumbers = new HashMap<StreamingTrack, Long>();
    protected Map<StreamingTrack, Long> sampleNumbers = new HashMap<StreamingTrack, Long>();
    long bytesWritten = 0;
    volatile boolean headerWritten = false;


    public StandardMp4Writer(List<StreamingTrack> source, WritableByteChannel sink) {
        this.source = source;
        this.sink = sink;
        for (StreamingTrack streamingTrack : source) {
            streamingTrack.setSampleSink(this);
            chunkNumbers.put(streamingTrack, 1L);
            sampleNumbers.put(streamingTrack, 1L);
        }

    }

    public void close() throws IOException {

    }

    private void sortTracks() {
        Collections.sort(source, new Comparator<StreamingTrack>() {
            public int compare(StreamingTrack o1, StreamingTrack o2) {
                // compare times and account for timestamps!
                long a = nextChunkWriteStartTime.get(o1) * o2.getTimescale();
                long b = nextChunkWriteStartTime.get(o2) * o1.getTimescale();
                double d = Math.signum(a - b);
                return (int) d;
            }
        });
    }

    protected void write(WritableByteChannel out, Box... boxes) throws IOException {
        for (Box box1 : boxes) {
            box1.getBox(out);
            bytesWritten += box1.getSize();
        }
    }

    /**
     * Tests if the currently received samples for a given track
     * are already a 'chunk' as we want to have it. The next
     * sample will not be part of the chunk
     * will be added to the fragment buffer later.
     *
     * @param streamingTrack track to test
     * @param next           the lastest samples
     * @return true if a chunk is to b e created.
     */
    protected boolean isChunkReady(StreamingTrack streamingTrack, StreamingSample next) {
        long ts = nextSampleStartTime.get(streamingTrack);
        long cfst = nextChunkCreateStartTime.get(streamingTrack);

        return (ts > cfst + 2 * streamingTrack.getTimescale());
        // chunk interleave of 2 seconds
    }

    protected void writeChunkContainer(ChunkContainer chunkContainer) throws IOException {
        TrackBox tb = trackBoxes.get(chunkContainer.streamingTrack);
        ChunkOffsetBox stco = Path.getPath(tb, "mdia[0]/minf[0]/stbl[0]/stco[0]");
        Mp4Arrays.copyOfAndAppend(stco.getChunkOffsets(), bytesWritten + 8);
        write(sink, chunkContainer.mdat);
    }

    public void acceptSample(StreamingSample streamingSample, StreamingTrack streamingTrack) throws IOException {
        long sampleNumber = sampleNumbers.get(streamingTrack);
        TrackBox tb = trackBoxes.get(streamingTrack);
        if (tb == null) {
            tb = new TrackBox();
            tb.addBox(createTkhd(streamingTrack));
            tb.addBox(createMdia(streamingTrack));
            trackBoxes.put(streamingTrack, tb);
        }

        // We might want to do that when the chunk is created to save memory copy
        SampleSizeBox stsz = Path.getPath(tb, "mdia[0]/minf[0]/stbl[0]/stsz[0]");
        assert stsz != null;
        stsz.setSampleSizes(Mp4Arrays.copyOfAndAppend(stsz.getSampleSizes(), streamingSample.getContent().limit()));
        TimeToSampleBox stts = Path.getPath(tb, "mdia[0]/minf[0]/stbl[0]/stts[0]");
        assert stts != null;
        if (stts.getEntries().isEmpty()) {
            stts.getEntries().add(new TimeToSampleBox.Entry(1, streamingSample.getDuration()));
        } else {
            TimeToSampleBox.Entry sttsEntry = stts.getEntries().get(stts.getEntries().size() - 1);
            if (sttsEntry.getDelta() == streamingSample.getDuration()) {
                sttsEntry.setCount(sttsEntry.getCount() + 1);
            }
        }
        SampleFlagsSampleExtension sampleFlagsSampleExtension = streamingSample.getSampleExtension(SampleFlagsSampleExtension.class);
        if (sampleFlagsSampleExtension != null && sampleFlagsSampleExtension.isSyncSample()) {
            SyncSampleBox stss = Path.getPath(tb, "mdia[0]/minf[0]/stbl[0]/stss[0]");
            assert stss != null;
            stss.setSampleNumber(Mp4Arrays.copyOfAndAppend(stss.getSampleNumber(), sampleNumber));
        }


        synchronized (OBJ) {
            // need to synchronized here - I don't want two headers written under any circumstances
            if (!headerWritten) {
                boolean allTracksAtLeastOneSample = true;
                for (StreamingTrack track : source) {
                    allTracksAtLeastOneSample &= (nextSampleStartTime.get(track) > 0 || track == streamingTrack);
                }
                if (allTracksAtLeastOneSample) {

                    headerWritten = true;
                }
            }
        }

        try {
            CountDownLatch cdl = congestionControl.get(streamingTrack);
            if (cdl.getCount() > 0) {
                cdl.await();
            }
        } catch (InterruptedException e) {
            // don't care just move on
        }

        if (isChunkReady(streamingTrack, streamingSample)) {

            ChunkContainer fragmentContainer = createChunkContainer(streamingTrack);
            //System.err.println("Creating fragment for " + streamingTrack);
            sampleBuffers.get(streamingTrack).clear();
            nextChunkCreateStartTime.put(streamingTrack, nextChunkCreateStartTime.get(streamingTrack) + fragmentContainer.duration);
            Queue<ChunkContainer> fragmentQueue = chunkBuffers.get(streamingTrack);
            fragmentQueue.add(fragmentContainer);
            synchronized (OBJ) {
                if (headerWritten && this.source.get(0) == streamingTrack) {

                    Queue<ChunkContainer> tracksFragmentQueue;
                    StreamingTrack currentStreamingTrack;
                    // This will write AT LEAST the currently created fragment and possibly a few more
                    while (!(tracksFragmentQueue = chunkBuffers.get(
                            (currentStreamingTrack = this.source.get(0))
                    )).isEmpty()) {
                        TrackBox trackBox = trackBoxes.get(currentStreamingTrack);

                        ChunkContainer currentFragmentContainer = tracksFragmentQueue.remove();

                        writeChunkContainer(currentFragmentContainer);

                        congestionControl.get(currentStreamingTrack).countDown();
                        long ts = nextChunkWriteStartTime.get(currentStreamingTrack) + currentFragmentContainer.duration;
                        nextChunkWriteStartTime.put(currentStreamingTrack, ts);
                        if (LOG.isLoggable(Level.FINE)) {
                            LOG.fine(currentStreamingTrack + " advanced to " + (double) ts / currentStreamingTrack.getTimescale());
                        }
                        sortTracks();
                    }
                } else {
                    if (fragmentQueue.size() > 10) {
                        // if there are more than 10 fragments in the queue we don't want more samples of this track
                        // System.err.println("Stopping " + streamingTrack);
                        congestionControl.put(streamingTrack, new CountDownLatch(fragmentQueue.size()));
                    }
                }
            }


        }


        sampleBuffers.get(streamingTrack).add(streamingSample);
        nextSampleStartTime.put(streamingTrack, nextSampleStartTime.get(streamingTrack) + streamingSample.getDuration());
        sampleNumbers.put(streamingTrack, sampleNumber + 1);
    }

    private ChunkContainer createChunkContainer(StreamingTrack streamingTrack) {
        List<StreamingSample> samples = sampleBuffers.get(streamingTrack);
        long chunkNumber = chunkNumbers.get(streamingTrack);
        chunkNumbers.put(streamingTrack, chunkNumber + 1);
        ChunkContainer cc = new ChunkContainer();
        cc.streamingTrack = streamingTrack;
        cc.mdat = new Mdat(samples);
        cc.duration = nextSampleStartTime.get(streamingTrack) - nextChunkCreateStartTime.get(streamingTrack);
        TrackBox tb = trackBoxes.get(streamingTrack);
        SampleToChunkBox stsc = Path.getPath(tb, "mdia[0]/minf[0]/stbl[0]/stsc[0]");
        if (stsc.getEntries().isEmpty()) {
            stsc.getEntries().add(new SampleToChunkBox.Entry(chunkNumber, samples.size(), 1));
        } else {
            SampleToChunkBox.Entry e = stsc.getEntries().get(stsc.getEntries().size() - 1);
            if (e.getSamplesPerChunk() != samples.size()) {
                stsc.getEntries().add(new SampleToChunkBox.Entry(chunkNumber, samples.size(), 1));
            }
        }

        return cc;
    }

    protected Box createMdhd(StreamingTrack streamingTrack) {
        MediaHeaderBox mdhd = new MediaHeaderBox();
        mdhd.setCreationTime(creationTime);
        mdhd.setModificationTime(creationTime);
        mdhd.setDuration(0);//no duration in moov for fragmented movies
        mdhd.setTimescale(streamingTrack.getTimescale());
        mdhd.setLanguage(streamingTrack.getLanguage());
        return mdhd;
    }

    private class Mdat implements Box {
        ArrayList<StreamingSample> samples;
        long size;

        public Mdat(List<StreamingSample> samples) {
            this.samples = new ArrayList<StreamingSample>(samples);
            size = 8;
            for (StreamingSample sample : samples) {
                size += sample.getContent().limit();
            }
        }

        public String getType() {
            return "mdat";
        }

        public long getSize() {
            return size;
        }

        public void getBox(WritableByteChannel writableByteChannel) throws IOException {
            writableByteChannel.write(ByteBuffer.wrap(new byte[]{
                    (byte) ((size & 0xff000000) >> 24),
                    (byte) ((size & 0xff0000) >> 16),
                    (byte) ((size & 0xff00) >> 8),
                    (byte) ((size & 0xff)),
                    109, 100, 97, 116, // mdat

            }));
            for (StreamingSample sample : samples) {
                writableByteChannel.write(sample.getContent());
            }


        }
    }

    private class ChunkContainer {
        Mdat mdat;
        StreamingTrack streamingTrack;
        long duration;
    }
}
