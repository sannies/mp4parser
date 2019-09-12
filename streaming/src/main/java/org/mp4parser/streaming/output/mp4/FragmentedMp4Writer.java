package org.mp4parser.streaming.output.mp4;

import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.streaming.StreamingSample;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.extensions.*;
import org.mp4parser.streaming.output.SampleSink;
import org.mp4parser.tools.IsoTypeWriter;
import org.mp4parser.tools.Mp4Arrays;
import org.mp4parser.tools.Mp4Math;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static org.mp4parser.tools.CastUtils.l2i;


/**
 * Creates a fragmented MP4 file consisting of a header [ftyp, moov], any number of fragments
 * [moof, mdat]+ and a footer [mfra].
 * The MultiTrackFragmentedMp4Writer is a passive component. It will only be active if one of the
 * source tracks pushes a sample via {@link #acceptSample(StreamingSample, StreamingTrack)}.
 * It has to be closed ({@link #close()}) actively to trigger the write of remaining buffered
 * samples and the footer.
 */
public class FragmentedMp4Writer extends DefaultBoxes implements SampleSink {
    public static final Object OBJ = new Object();
    private static Logger LOG = LoggerFactory.getLogger(FragmentedMp4Writer.class.getName());
    protected final WritableByteChannel sink;
    protected List<StreamingTrack> source;
    protected Date creationTime;
    protected long sequenceNumber = 1;
    protected Map<StreamingTrack, CountDownLatch> congestionControl = new ConcurrentHashMap<StreamingTrack, CountDownLatch>();
    /**
     * Contains the start time of the next segment in line that will be created.
     */
    protected Map<StreamingTrack, Long> nextFragmentCreateStartTime = new ConcurrentHashMap<StreamingTrack, Long>();
    /**
     * Contains the start time of the next segment in line that will be written.
     */
    protected Map<StreamingTrack, Long> nextFragmentWriteStartTime = new ConcurrentHashMap<StreamingTrack, Long>();
    /**
     * Contains the next sample's start time.
     */
    protected Map<StreamingTrack, Long> nextSampleStartTime = new HashMap<StreamingTrack, Long>();
    /**
     * Buffers the samples per track until there are enough samples to form a Segment.
     */
    protected Map<StreamingTrack, List<StreamingSample>> sampleBuffers = new HashMap<StreamingTrack, List<StreamingSample>>();
    /**
     * Buffers segements until it's time for a segment to be written.
     */
    protected Map<StreamingTrack, Queue<FragmentContainer>> fragmentBuffers = new ConcurrentHashMap<StreamingTrack, Queue<FragmentContainer>>();
    protected Map<StreamingTrack, long[]> tfraOffsets = new HashMap<StreamingTrack, long[]>();
    protected Map<StreamingTrack, long[]> tfraTimes = new HashMap<StreamingTrack, long[]>();
    long bytesWritten = 0;
    volatile boolean headerWritten = false;

    public FragmentedMp4Writer(List<StreamingTrack> source, WritableByteChannel sink) throws IOException {
        this.source = new LinkedList<StreamingTrack>(source);
        this.sink = sink;
        this.creationTime = new Date();
        HashSet<Long> trackIds = new HashSet<Long>();
        for (StreamingTrack streamingTrack : source) {
            // this connects sample source with sample sink
            streamingTrack.setSampleSink(this);
            sampleBuffers.put(streamingTrack, new ArrayList<StreamingSample>());
            fragmentBuffers.put(streamingTrack, new LinkedList<FragmentContainer>());
            nextFragmentCreateStartTime.put(streamingTrack, 0L);
            nextFragmentWriteStartTime.put(streamingTrack, 0L);
            nextSampleStartTime.put(streamingTrack, 0L);
            congestionControl.put(streamingTrack, new CountDownLatch(0));
            if (streamingTrack.getTrackExtension(TrackIdTrackExtension.class) != null) {
                TrackIdTrackExtension trackIdTrackExtension = streamingTrack.getTrackExtension(TrackIdTrackExtension.class);
                assert trackIdTrackExtension != null;
                if (trackIds.contains(trackIdTrackExtension.getTrackId())) {
                    throw new IOException("There may not be two tracks with the same trackID within one file");
                }
            }
        }
        for (StreamingTrack streamingTrack : source) {
            if (streamingTrack.getTrackExtension(TrackIdTrackExtension.class) == null) {
                long maxTrackId = 0;
                for (Long trackId : trackIds) {
                    maxTrackId = Math.max(trackId, maxTrackId);
                }
                TrackIdTrackExtension tiExt = new TrackIdTrackExtension(maxTrackId + 1);
                trackIds.add(tiExt.getTrackId());
                streamingTrack.addTrackExtension(tiExt);
            }
        }

    }

    /**
     * Writes the remaining samples to file (even though the typical condition for wrapping up
     * a segment have not yet been met) and writes the MovieFragmentRandomAccessBox.
     * It does not close the sink!
     *
     * @throws IOException if writing to the underlying data sink fails
     * @see MovieFragmentRandomAccessBox
     */
    public synchronized void close() throws IOException {
        for (StreamingTrack streamingTrack : source) {
            writeFragment(createFragment(streamingTrack, sampleBuffers.get(streamingTrack)));
            streamingTrack.close();
        }
        writeFooter(createFooter());
    }

    protected void write(WritableByteChannel out, Box... boxes) throws IOException {
        for (Box box1 : boxes) {
            box1.getBox(out);
            bytesWritten += box1.getSize();
        }
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


    protected Box createMvex() {
        MovieExtendsBox mvex = new MovieExtendsBox();
        final MovieExtendsHeaderBox mved = new MovieExtendsHeaderBox();
        mved.setVersion(1);

        mved.setFragmentDuration(0);

        mvex.addBox(mved);
        for (StreamingTrack streamingTrack : source) {
            mvex.addBox(createTrex(streamingTrack));
        }
        return mvex;
    }

    protected Box createTrex(StreamingTrack streamingTrack) {
        TrackExtendsBox trex = new TrackExtendsBox();
        trex.setTrackId(streamingTrack.getTrackExtension(TrackIdTrackExtension.class).getTrackId());
        trex.setDefaultSampleDescriptionIndex(1);
        trex.setDefaultSampleDuration(0);
        trex.setDefaultSampleSize(0);
        SampleFlags sf = new SampleFlags();

        trex.setDefaultSampleFlags(sf);
        return trex;
    }

    protected Box createMvhd() {
        MovieHeaderBox mvhd = new MovieHeaderBox();
        mvhd.setVersion(1);
        mvhd.setCreationTime(creationTime);
        mvhd.setModificationTime(creationTime);
        mvhd.setDuration(0);//no duration in moov for fragmented movies

        long[] timescales = new long[0];
        long maxTrackId = 0;
        for (StreamingTrack streamingTrack : source) {
            timescales = Mp4Arrays.copyOfAndAppend(timescales, streamingTrack.getTimescale());
            maxTrackId = Math.max(streamingTrack.getTrackExtension(TrackIdTrackExtension.class).getTrackId(), maxTrackId);
        }

        mvhd.setTimescale(Mp4Math.lcm(timescales));
        // find the next available trackId
        mvhd.setNextTrackId(maxTrackId + 1);
        return mvhd;
    }


    protected Box createMoov() {
        MovieBox movieBox = new MovieBox();

        movieBox.addBox(createMvhd());

        for (StreamingTrack streamingTrack : source) {
            movieBox.addBox(createTrak(streamingTrack));
        }
        movieBox.addBox(createMvex());

        // metadata here
        return movieBox;
    }

    protected Box[] createHeader() {
        return new Box[]{createFtyp(), createMoov()};
    }

    private void sortTracks() {
        Collections.sort(source, new Comparator<StreamingTrack>() {
            public int compare(StreamingTrack o1, StreamingTrack o2) {
                // compare times and account for timestamps!
                long a = nextFragmentWriteStartTime.get(o1) * o2.getTimescale();
                long b = nextFragmentWriteStartTime.get(o2) * o1.getTimescale();
                double d = Math.signum(a - b);
                return (int) d;
            }
        });
    }

    public void acceptSample(StreamingSample streamingSample, StreamingTrack streamingTrack) throws IOException {

        synchronized (OBJ) {
            // need to synchronized here - I don't want two headers written under any circumstances
            if (!headerWritten) {
                boolean allTracksAtLeastOneSample = true;
                for (StreamingTrack track : source) {
                    allTracksAtLeastOneSample &= (nextSampleStartTime.get(track) > 0 || track == streamingTrack);
                }
                if (allTracksAtLeastOneSample) {

                    writeHeader(createHeader());
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

        if (isFragmentReady(streamingTrack, streamingSample)) {

            FragmentContainer fragmentContainer = createFragmentContainer(streamingTrack);
            //System.err.println("Creating fragment for " + streamingTrack);
            sampleBuffers.get(streamingTrack).clear();
            nextFragmentCreateStartTime.put(streamingTrack, nextFragmentCreateStartTime.get(streamingTrack) + fragmentContainer.duration);
            Queue<FragmentContainer> fragmentQueue = fragmentBuffers.get(streamingTrack);
            fragmentQueue.add(fragmentContainer);
            synchronized (OBJ) {
                if (headerWritten && this.source.get(0) == streamingTrack) {

                    Queue<FragmentContainer> tracksFragmentQueue;
                    StreamingTrack currentStreamingTrack;
                    // This will write AT LEAST the currently created fragment and possibly a few more
                    while (!(tracksFragmentQueue = fragmentBuffers.get(
                            (currentStreamingTrack = this.source.get(0))
                    )).isEmpty()) {

                        FragmentContainer currentFragmentContainer = tracksFragmentQueue.remove();

                        writeFragment(currentFragmentContainer.fragmentContent);

                        congestionControl.get(currentStreamingTrack).countDown();
                        long ts = nextFragmentWriteStartTime.get(currentStreamingTrack) + currentFragmentContainer.duration;
                        nextFragmentWriteStartTime.put(currentStreamingTrack, ts);
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(currentStreamingTrack + " advanced to " + (double) ts / currentStreamingTrack.getTimescale());
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


    }

    /**
     * Tests if the currently received samples for a given track
     * form a valid fragment taking the latest received sample into
     * account. The next sample is not part of the segment and
     * will be added to the fragment buffer later.
     *
     * @param streamingTrack track to test
     * @param next           the lastest samples
     * @return true if a fragment has been created.
     */
    protected boolean isFragmentReady(StreamingTrack streamingTrack, StreamingSample next) {
        long ts = nextSampleStartTime.get(streamingTrack);
        long cfst = nextFragmentCreateStartTime.get(streamingTrack);

        if ((ts > cfst + 3 * streamingTrack.getTimescale())) {
            // mininum fragment length == 3 seconds
            SampleFlagsSampleExtension sfExt = next.getSampleExtension(SampleFlagsSampleExtension.class);
            if (sfExt == null || sfExt.isSyncSample()) {
                //System.err.println(streamingTrack + " ready at " + ts);
                // the next sample needs to be a sync sample
                // when there is no SampleFlagsSampleExtension we assume syncSample == true
                return true;
            }
        }
        return false;
    }

    protected Box[] createFragment(StreamingTrack streamingTrack, List<StreamingSample> samples) {
        nextFragmentCreateStartTime.get(streamingTrack);
        tfraOffsets.put(streamingTrack, Mp4Arrays.copyOfAndAppend(tfraOffsets.get(streamingTrack), bytesWritten));
        tfraTimes.put(streamingTrack, Mp4Arrays.copyOfAndAppend(tfraTimes.get(streamingTrack), nextFragmentCreateStartTime.get(streamingTrack)));

        LOG.trace("Container created");
        Box moof = createMoof(streamingTrack, samples);
        LOG.trace("moof created");
        Box mdat = createMdat(samples);
        LOG.trace("mdat created");

        if (LOG.isDebugEnabled()) {
            double duration = nextSampleStartTime.get(streamingTrack) - nextFragmentCreateStartTime.get(streamingTrack);
            LOG.debug("created fragment for " + streamingTrack + " of " + (duration / streamingTrack.getTimescale()) + " seconds");
        }
        return new Box[]{moof, mdat};
    }

    private FragmentContainer createFragmentContainer(StreamingTrack streamingTrack) {
        FragmentContainer fragmentContainer = new FragmentContainer();
        List<StreamingSample> samples = new ArrayList<StreamingSample>(sampleBuffers.get(streamingTrack));
        fragmentContainer.fragmentContent = createFragment(streamingTrack, samples);
        fragmentContainer.duration = nextSampleStartTime.get(streamingTrack) - nextFragmentCreateStartTime.get(streamingTrack);
        return fragmentContainer;
    }


    /**
     * Writes the given boxes. It's called as soon as the MultiTrackFragmentedMp4Writer
     * received a sample from each source as this is the first point in time where the
     * MultiTrackFragmentedMp4Writer can be sure that all config data is available from
     * the sources.
     * It typically writes a ftyp/moov pair but will write what ever
     * the boxes argument contains
     *
     * @param boxes any number of boxes that form the header
     * @throws IOException when writing to the sink fails.
     * @see FileTypeBox
     * @see ProgressiveDownloadInformationBox
     * @see MovieBox
     * @see SegmentIndexBox
     */
    protected void writeHeader(Box... boxes) throws IOException {
        write(sink, boxes);
    }

    /**
     * Writes the given boxes. It's called as soon as a fragment is created.
     * It typically write a single moof/mdat pair but will write what ever
     * the boxes argument contains
     *
     * @param boxes any number of boxes that form fragment
     * @throws IOException when writing to the sink fails.
     * @see MovieFragmentBox
     * @see MediaDataBox
     * @see SegmentTypeBox
     * @see SegmentIndexBox
     */
    protected void writeFragment(Box... boxes) throws IOException {
        write(sink, boxes);
    }

    /**
     * Writes the given boxes. It's called as last write operation. Typically the only
     * box written is the MovieFragmentRandomAccessBox.
     *
     * @param boxes any number of boxes to conclude the file.
     * @throws IOException when writing to the sink fails.
     * @see MovieFragmentRandomAccessBox
     */
    protected void writeFooter(Box... boxes) throws IOException {
        write(sink, boxes);
    }


    private Box createMoof(StreamingTrack streamingTrack, List<StreamingSample> samples) {
        MovieFragmentBox moof = new MovieFragmentBox();
        createMfhd(sequenceNumber, moof);
        createTraf(streamingTrack, moof, samples);
        TrackRunBox firstTrun = moof.getTrackRunBoxes().get(0);
        firstTrun.setDataOffset(1); // dummy to make size correct
        firstTrun.setDataOffset((int) (8 + moof.getSize())); // mdat header + moof size
        return moof;

    }

    protected void createTfhd(StreamingTrack streamingTrack, TrackFragmentBox parent) {
        TrackFragmentHeaderBox tfhd = new TrackFragmentHeaderBox();
        SampleFlags sf = new SampleFlags();
        DefaultSampleFlagsTrackExtension defaultSampleFlagsTrackExtension = streamingTrack.getTrackExtension(DefaultSampleFlagsTrackExtension.class);
        // I don't like the idea of using sampleflags in trex as it breaks the "self-contained" property of a fragment
        if (defaultSampleFlagsTrackExtension != null) {
            sf.setIsLeading(defaultSampleFlagsTrackExtension.getIsLeading());
            sf.setSampleIsDependedOn(defaultSampleFlagsTrackExtension.getSampleIsDependedOn());
            sf.setSampleDependsOn(defaultSampleFlagsTrackExtension.getSampleDependsOn());
            sf.setSampleHasRedundancy(defaultSampleFlagsTrackExtension.getSampleHasRedundancy());
            sf.setSampleIsDifferenceSample(defaultSampleFlagsTrackExtension.isSampleIsNonSyncSample());
            sf.setSamplePaddingValue(defaultSampleFlagsTrackExtension.getSamplePaddingValue());
            sf.setSampleDegradationPriority(defaultSampleFlagsTrackExtension.getSampleDegradationPriority());

        }
        tfhd.setDefaultSampleFlags(sf);
        tfhd.setBaseDataOffset(-1);
        tfhd.setTrackId(streamingTrack.getTrackExtension(TrackIdTrackExtension.class).getTrackId());
        tfhd.setDefaultBaseIsMoof(true);
        parent.addBox(tfhd);
    }

    protected void createTfdt(StreamingTrack streamingTrack, TrackFragmentBox parent) {
        TrackFragmentBaseMediaDecodeTimeBox tfdt = new TrackFragmentBaseMediaDecodeTimeBox();
        tfdt.setVersion(1);
        tfdt.setBaseMediaDecodeTime(nextFragmentCreateStartTime.get(streamingTrack));
        parent.addBox(tfdt);
    }

    protected void createTrun(StreamingTrack streamingTrack, TrackFragmentBox parent, List<StreamingSample> samples) {
        TrackRunBox trun = new TrackRunBox();
        trun.setVersion(1);
        trun.setSampleDurationPresent(true);
        trun.setSampleSizePresent(true);
        List<TrackRunBox.Entry> entries = new ArrayList<TrackRunBox.Entry>(samples.size());


        trun.setSampleCompositionTimeOffsetPresent(streamingTrack.getTrackExtension(CompositionTimeTrackExtension.class) != null);

        DefaultSampleFlagsTrackExtension defaultSampleFlagsTrackExtension = streamingTrack.getTrackExtension(DefaultSampleFlagsTrackExtension.class);
        trun.setSampleFlagsPresent(defaultSampleFlagsTrackExtension == null);

        for (StreamingSample streamingSample : samples) {
            TrackRunBox.Entry entry = new TrackRunBox.Entry();
            entry.setSampleSize(streamingSample.getContent().remaining());
            if (defaultSampleFlagsTrackExtension == null) {
                SampleFlagsSampleExtension sampleFlagsSampleExtension = streamingSample.getSampleExtension(SampleFlagsSampleExtension.class);
                assert sampleFlagsSampleExtension != null : "SampleDependencySampleExtension missing even though SampleDependencyTrackExtension was present";
                SampleFlags sflags = new SampleFlags();
                sflags.setIsLeading(sampleFlagsSampleExtension.getIsLeading());
                sflags.setSampleIsDependedOn(sampleFlagsSampleExtension.getSampleIsDependedOn());
                sflags.setSampleDependsOn(sampleFlagsSampleExtension.getSampleDependsOn());
                sflags.setSampleHasRedundancy(sampleFlagsSampleExtension.getSampleHasRedundancy());
                sflags.setSampleIsDifferenceSample(sampleFlagsSampleExtension.isSampleIsNonSyncSample());
                sflags.setSamplePaddingValue(sampleFlagsSampleExtension.getSamplePaddingValue());
                sflags.setSampleDegradationPriority(sampleFlagsSampleExtension.getSampleDegradationPriority());

                entry.setSampleFlags(sflags);

            }

            entry.setSampleDuration(streamingSample.getDuration());

            if (trun.isSampleCompositionTimeOffsetPresent()) {
                CompositionTimeSampleExtension compositionTimeSampleExtension = streamingSample.getSampleExtension(CompositionTimeSampleExtension.class);
                assert compositionTimeSampleExtension != null : "CompositionTimeSampleExtension missing even though CompositionTimeTrackExtension was present";
                entry.setSampleCompositionTimeOffset(l2i(compositionTimeSampleExtension.getCompositionTimeOffset()));
            }

            entries.add(entry);
        }

        trun.setEntries(entries);

        parent.addBox(trun);
    }

    private void createTraf(StreamingTrack streamingTrack, MovieFragmentBox moof, List<StreamingSample> samples) {
        TrackFragmentBox traf = new TrackFragmentBox();
        moof.addBox(traf);
        createTfhd(streamingTrack, traf);
        createTfdt(streamingTrack, traf);
        createTrun(streamingTrack, traf, samples);

        if (streamingTrack.getTrackExtension(CencEncryptTrackExtension.class) != null) {
            //     createSaiz(getTrackExtension(source, CencEncryptTrackExtension.class), sequenceNumber, traf);
            //     createSenc(getTrackExtension(source, CencEncryptTrackExtension.class), sequenceNumber, traf);
            //     createSaio(getTrackExtension(source, CencEncryptTrackExtension.class), sequenceNumber, traf);
        }


  /*      Map<String, List<GroupEntry>> groupEntryFamilies = new HashMap<String, List<GroupEntry>>();
        for (Map.Entry<GroupEntry, long[]> sg : track.getSampleGroups().entrySet()) {
            String type = sg.getKey().getType();
            List<GroupEntry> groupEntries = groupEntryFamilies.get(type);
            if (groupEntries == null) {
                groupEntries = new ArrayList<GroupEntry>();
                groupEntryFamilies.put(type, groupEntries);
            }
            groupEntries.add(sg.getKey());
        }


        for (Map.Entry<String, List<GroupEntry>> sg : groupEntryFamilies.entrySet()) {
            SampleGroupDescriptionBox sgpd = new SampleGroupDescriptionBox();
            String type = sg.getKey();
            sgpd.setGroupEntries(sg.getValue());
            SampleToGroupBox sbgp = new SampleToGroupBox();
            sbgp.setGroupingType(type);
            SampleToGroupBox.Entry last = null;
            for (int i = l2i(startSample - 1); i < l2i(endSample - 1); i++) {
                int index = 0;
                for (int j = 0; j < sg.getValue().size(); j++) {
                    GroupEntry groupEntry = sg.getValue().get(j);
                    long[] sampleNums = track.getSampleGroups().get(groupEntry);
                    if (Arrays.binarySearch(sampleNums, i) >= 0) {
                        index = j + 1;
                    }
                }
                if (last == null || last.getGroupDescriptionIndex() != index) {
                    last = new SampleToGroupBox.Entry(1, index);
                    sbgp.getEntries().add(last);
                } else {
                    last.setSampleCount(last.getSampleCount() + 1);
                }
            }
            traf.addBox(sgpd);
            traf.addBox(sbgp);
        }*/

    }

    protected Box[] createFooter() {
        MovieFragmentRandomAccessBox mfra = new MovieFragmentRandomAccessBox();

        for (StreamingTrack track : source) {
            mfra.addBox(createTfra(track));
        }

        MovieFragmentRandomAccessOffsetBox mfro = new MovieFragmentRandomAccessOffsetBox();
        mfra.addBox(mfro);
        mfro.setMfraSize(mfra.getSize());
        return new Box[]{mfra};
    }

    /**
     * Creates a 'tfra' - track fragment random access box for the given track with the isoFile.
     * The tfra contains a map of random access points with time as key and offset within the isofile
     * as value.
     *
     * @param track the concerned track
     * @return a track fragment random access box.
     */
    protected Box createTfra(StreamingTrack track) {
        TrackFragmentRandomAccessBox tfra = new TrackFragmentRandomAccessBox();
        tfra.setVersion(1); // use long offsets and times
        long[] offsets = tfraOffsets.get(track);
        long[] times = tfraTimes.get(track);
        List<TrackFragmentRandomAccessBox.Entry> entries = new ArrayList<TrackFragmentRandomAccessBox.Entry>(times.length);
        for (int i = 0; i < times.length; i++) {
            entries.add(new TrackFragmentRandomAccessBox.Entry(times[i], offsets[i], 1, 1, 1));
        }


        tfra.setEntries(entries);
        tfra.setTrackId(track.getTrackExtension(TrackIdTrackExtension.class).getTrackId());
        return tfra;
    }


    private void createMfhd(long sequenceNumber, MovieFragmentBox moof) {
        MovieFragmentHeaderBox mfhd = new MovieFragmentHeaderBox();
        mfhd.setSequenceNumber(sequenceNumber);
        moof.addBox(mfhd);
    }

    private Box createMdat(final List<StreamingSample> samples) {

        return new Box() {
            public String getType() {
                return "mdat";
            }

            public long getSize() {
                long l = 8;
                for (StreamingSample streamingSample : samples) {
                    l += streamingSample.getContent().limit();
                }
                return l;
            }

            public void getBox(WritableByteChannel writableByteChannel) throws IOException {
                long l = 8;
                for (StreamingSample streamingSample : samples) {
                    ByteBuffer sampleContent = streamingSample.getContent();
                    l += sampleContent.limit();
                }
                ByteBuffer bb = ByteBuffer.allocate(8);
                IsoTypeWriter.writeUInt32(bb, l);
                bb.put(IsoFile.fourCCtoBytes(getType()));
                writableByteChannel.write((ByteBuffer) ((Buffer)bb).rewind());

                for (StreamingSample streamingSample : samples) {
                    writableByteChannel.write((ByteBuffer) ((Buffer)streamingSample.getContent()).rewind());
                }
            }

        };
    }

    public class FragmentContainer {
        Box[] fragmentContent;
        long duration;
    }
}
