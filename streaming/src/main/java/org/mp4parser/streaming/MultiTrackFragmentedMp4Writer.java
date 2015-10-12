package org.mp4parser.streaming;

import org.mp4parser.*;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.streaming.extensions.*;
import org.mp4parser.tools.IsoTypeWriter;
import org.mp4parser.tools.Mp4Arrays;
import org.mp4parser.tools.Mp4Math;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.logging.Logger;

import static org.mp4parser.tools.CastUtils.l2i;


/**
 *
 */
public class MultiTrackFragmentedMp4Writer implements SampleSink {
    private static final Logger LOG = Logger.getLogger(MultiTrackFragmentedMp4Writer.class.getName());

    protected final OutputStream outputStream;
    protected List<StreamingTrack> source;

    protected Date creationTime;
    protected Map<StreamingTrack, List<StreamingSample>> fragmentBuffers = new HashMap<StreamingTrack, List<StreamingSample>>();
    protected long sequenceNumber = 1;
    protected Map<StreamingTrack, Long> currentFragmentStartTime = new HashMap<StreamingTrack, Long>();
    protected Map<StreamingTrack, Long> currentTime = new HashMap<StreamingTrack, Long>();

    protected Map<StreamingTrack, long[]> tfraOffsets = new HashMap<StreamingTrack, long[]>();
    protected Map<StreamingTrack, long[]> tfraTimes = new HashMap<StreamingTrack, long[]>();
    protected boolean closed = false;
    long bytesWritten = 0;
    boolean headerWritten = false;


    public MultiTrackFragmentedMp4Writer(List<StreamingTrack> source, OutputStream outputStream) throws IOException {
        this.source = new LinkedList<StreamingTrack>(source);
        for (StreamingTrack streamingTrack : source) {
            streamingTrack.setSampleSink(this);
        }
        this.outputStream = outputStream;
        this.creationTime = new Date();
        HashSet<Long> trackIds = new HashSet<Long>();
        for (StreamingTrack streamingTrack : source) {
            fragmentBuffers.put(streamingTrack, new ArrayList<StreamingSample>());
            currentFragmentStartTime.put(streamingTrack, 0L);
            currentTime.put(streamingTrack, 0L);
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

    public void close() throws IOException {
        this.closed = true;


        for (StreamingTrack streamingTrack : source) {
            writeFragment(createFragment(streamingTrack, fragmentBuffers.get(streamingTrack)));
            streamingTrack.close();
        }

        writeMovieFragmentRandomAccess(createMfra());
    }

    public void writeMovieFragmentRandomAccess(Box mfra) throws IOException {
        WritableByteChannel out = Channels.newChannel(outputStream);
        mfra.getBox(out);
    }

    protected ParsableBox createMvhd() {
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

    protected ParsableBox createMdiaHdlr(StreamingTrack streamingTrack) {
        HandlerBox hdlr = new HandlerBox();
        hdlr.setHandlerType(streamingTrack.getHandler());
        return hdlr;
    }

    protected ParsableBox createMdhd(StreamingTrack streamingTrack) {
        MediaHeaderBox mdhd = new MediaHeaderBox();
        mdhd.setCreationTime(creationTime);
        mdhd.setModificationTime(creationTime);
        mdhd.setDuration(0);//no duration in moov for fragmented movies
        mdhd.setTimescale(streamingTrack.getTimescale());
        mdhd.setLanguage(streamingTrack.getLanguage());
        return mdhd;
    }

    protected ParsableBox createMdia(StreamingTrack streamingTrack) {
        MediaBox mdia = new MediaBox();
        mdia.addBox(createMdhd(streamingTrack));
        mdia.addBox(createMdiaHdlr(streamingTrack));
        mdia.addBox(createMinf(streamingTrack));
        return mdia;
    }

    protected ParsableBox createMinf(StreamingTrack streamingTrack) {
        MediaInformationBox minf = new MediaInformationBox();
        if (streamingTrack.getHandler().equals("vide")) {
            minf.addBox(new VideoMediaHeaderBox());
        } else if (streamingTrack.getHandler().equals("soun")) {
            minf.addBox(new SoundMediaHeaderBox());
        } else if (streamingTrack.getHandler().equals("text")) {
            minf.addBox(new NullMediaHeaderBox());
        } else if (streamingTrack.getHandler().equals("subt")) {
            minf.addBox(new SubtitleMediaHeaderBox());
        } else if (streamingTrack.getHandler().equals("hint")) {
            minf.addBox(new HintMediaHeaderBox());
        } else if (streamingTrack.getHandler().equals("sbtl")) {
            minf.addBox(new NullMediaHeaderBox());
        }
        minf.addBox(createDinf());
        minf.addBox(createStbl(streamingTrack));
        return minf;
    }

    protected ParsableBox createStbl(StreamingTrack streamingTrack) {
        SampleTableBox stbl = new SampleTableBox();

        stbl.addBox(streamingTrack.getSampleDescriptionBox());
        stbl.addBox(new TimeToSampleBox());
        stbl.addBox(new SampleToChunkBox());
        stbl.addBox(new SampleSizeBox());
        stbl.addBox(new StaticChunkOffsetBox());
        return stbl;
    }

    protected DataInformationBox createDinf() {
        DataInformationBox dinf = new DataInformationBox();
        DataReferenceBox dref = new DataReferenceBox();
        dinf.addBox(dref);
        DataEntryUrlBox url = new DataEntryUrlBox();
        url.setFlags(1);
        dref.addBox(url);
        return dinf;
    }

    protected ParsableBox createTrak(StreamingTrack streamingTrack) {
        TrackBox trackBox = new TrackBox();
        trackBox.addBox(createTkhd(streamingTrack));
        trackBox.addBox(createMdia(streamingTrack));
        return trackBox;
    }

    private Box createTkhd(StreamingTrack streamingTrack) {
        TrackHeaderBox tkhd = new TrackHeaderBox();
        tkhd.setTrackId(streamingTrack.getTrackExtension(TrackIdTrackExtension.class).getTrackId());
        DimensionTrackExtension dte = streamingTrack.getTrackExtension(DimensionTrackExtension.class);
        if (dte != null) {
            tkhd.setHeight(dte.getHeight());
            tkhd.setWidth(dte.getWidth());
        }
        return tkhd;
    }

    public ParsableBox createFtyp() {
        List<String> minorBrands = new LinkedList<String>();
        minorBrands.add("isom");
        minorBrands.add("iso2");
        minorBrands.add("avc1");
        minorBrands.add("iso6");
        minorBrands.add("mp41");
        return new FileTypeBox("isom", 512, minorBrands);
    }

    protected ParsableBox createMvex() {
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

    protected ParsableBox createTrex(StreamingTrack streamingTrack) {
        TrackExtendsBox trex = new TrackExtendsBox();
        trex.setTrackId(streamingTrack.getTrackExtension(TrackIdTrackExtension.class).getTrackId());
        trex.setDefaultSampleDescriptionIndex(1);
        trex.setDefaultSampleDuration(0);
        trex.setDefaultSampleSize(0);
        SampleFlags sf = new SampleFlags();

        trex.setDefaultSampleFlags(sf);
        return trex;
    }

    protected ParsableBox createMoov() {
        MovieBox movieBox = new MovieBox();

        movieBox.addBox(createMvhd());

        for (StreamingTrack streamingTrack : source) {
            movieBox.addBox(createTrak(streamingTrack));
        }
        movieBox.addBox(createMvex());

        // metadata here
        return movieBox;
    }

    protected void writeHeader(Container container) throws IOException {
        final WritableByteChannel out = Channels.newChannel(outputStream);
        for (Box box : container.getBoxes()) {
            box.getBox(out);
            bytesWritten += box.getSize();
        }
    }

    protected Container createHeader() {
        Container b = new BasicContainer();
        b.getBoxes().add(createFtyp());
        b.getBoxes().add(createMoov());
        return b;
    }

    private void sortTracks() {
        Collections.sort(source, new Comparator<StreamingTrack>() {
            public int compare(StreamingTrack o1, StreamingTrack o2) {
                return currentFragmentStartTime.get(o1).compareTo(currentFragmentStartTime.get(o2));
            }
        });
    }

    public synchronized void acceptSample(StreamingSample streamingSample, StreamingTrack streamingTrack) throws IOException {
        if (!headerWritten) {
            boolean allTracksAtLeastOneSample = true;
            for (StreamingTrack track : source) {
                allTracksAtLeastOneSample &= (currentTime.get(track) > 0 || track == streamingTrack);
            }
            if (allTracksAtLeastOneSample) {
                writeHeader(createHeader());
                headerWritten = true;
            }
        }

        fragmentBuffers.get(streamingTrack).add(streamingSample);
        currentTime.put(streamingTrack, currentTime.get(streamingTrack) + streamingSample.getDuration());

        if (this.source.get(0) == streamingTrack) {
            // we might have a fragment to write if
            while (true) {
                if (!(emitFragment())) break;
            }
        }
    }


    protected boolean emitFragment() throws IOException {
        StreamingTrack streamingTrack = source.get(0);
        // As the fragment start times need to increase fragment by fragment
        // wo only have to look at the first track in this list as it is always
        // sorted by next fragment start time.
        long ts = currentTime.get(streamingTrack);
        long cfst = currentFragmentStartTime.get(streamingTrack);

        if ((ts > cfst + 3 * streamingTrack.getTimescale())) {
            List<StreamingSample> fragmentCandidates = fragmentBuffers.get(streamingTrack);
            List<StreamingSample> inFragment = new ArrayList<StreamingSample>(fragmentCandidates.size());
            long time = 0;
            boolean found = false;
            for (StreamingSample fragmentCandidate : fragmentCandidates) {
                if (time > 3 * streamingTrack.getTimescale()) {
                    SampleFlagsSampleExtension sampleFlagsSampleExtension = fragmentCandidate.getSampleExtension(SampleFlagsSampleExtension.class);
                    if (sampleFlagsSampleExtension == null || sampleFlagsSampleExtension.isSyncSample()) {
                        // I'll assume that we have a sync sample if we don't have any sample flag extension
                        found = true;
                        break;
                    }
                }
                time += (double) fragmentCandidate.getDuration();
                inFragment.add(fragmentCandidate);
            }
            if (found) {
                fragmentCandidates.removeAll(inFragment);
                writeFragment(createFragment(streamingTrack, inFragment));
                currentFragmentStartTime.put(streamingTrack, currentFragmentStartTime.get(streamingTrack) + time);
                sortTracks();
                return true;
            }

        }
        return false;
    }


    protected Container createFragment(StreamingTrack streamingTrack, List<StreamingSample> samples) throws IOException {
        currentFragmentStartTime.get(streamingTrack);
        tfraOffsets.put(streamingTrack, Mp4Arrays.copyOfAndAppend(tfraOffsets.get(streamingTrack), bytesWritten));
        tfraTimes.put(streamingTrack, Mp4Arrays.copyOfAndAppend(tfraTimes.get(streamingTrack), currentFragmentStartTime.get(streamingTrack)));

        Container b = new BasicContainer();
        LOG.finest("Container created");
        b.getBoxes().add(createMoof(streamingTrack, samples));
        LOG.finest("moof created");
        b.getBoxes().add(createMdat(samples));
        LOG.finest("mdat created");
        return b;
    }

    protected void writeFragment(Container fragment) throws IOException {
        WritableByteChannel out = Channels.newChannel(outputStream);
        for (Box box : fragment.getBoxes()) {
            box.getBox(out);
            bytesWritten += box.getSize();
        }
    }


    private ParsableBox createMoof(StreamingTrack streamingTrack, List<StreamingSample> samples) {

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
        tfdt.setBaseMediaDecodeTime(currentFragmentStartTime.get(streamingTrack));
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

    protected ParsableBox createMfra() {
        MovieFragmentRandomAccessBox mfra = new MovieFragmentRandomAccessBox();

        for (StreamingTrack track : source) {
            mfra.addBox(createTfra(track));
        }

        MovieFragmentRandomAccessOffsetBox mfro = new MovieFragmentRandomAccessOffsetBox();
        mfra.addBox(mfro);
        mfro.setMfraSize(mfra.getSize());
        return mfra;
    }

    /**
     * Creates a 'tfra' - track fragment random access box for the given track with the isoFile.
     * The tfra contains a map of random access points with time as key and offset within the isofile
     * as value.
     *
     * @param track the concerned track
     * @return a track fragment random access box.
     */
    protected ParsableBox createTfra(StreamingTrack track) {
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
                    l += streamingSample.getContent().remaining();
                }
                return l;
            }

            public void getBox(WritableByteChannel writableByteChannel) throws IOException {
                ArrayList<ByteBuffer> sampleContents = new ArrayList<ByteBuffer>();
                long l = 8;
                for (StreamingSample streamingSample : samples) {
                    ByteBuffer sampleContent = streamingSample.getContent();
                    sampleContents.add(sampleContent);
                    l += sampleContent.remaining();
                }
                ByteBuffer bb = ByteBuffer.allocate(8);
                IsoTypeWriter.writeUInt32(bb, l);
                bb.put(IsoFile.fourCCtoBytes(getType()));
                writableByteChannel.write((ByteBuffer) bb.rewind());

                for (ByteBuffer sampleContent : sampleContents) {
                    writableByteChannel.write(sampleContent);
                }
            }

        };
    }
}
