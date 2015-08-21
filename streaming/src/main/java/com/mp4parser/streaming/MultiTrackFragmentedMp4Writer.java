package com.mp4parser.streaming;

import com.mp4parser.Box;
import com.mp4parser.IsoFile;
import com.mp4parser.ParsableBox;
import com.mp4parser.boxes.iso14496.part12.*;
import com.mp4parser.muxer.Sample;
import com.mp4parser.streaming.extensions.*;
import com.mp4parser.tools.IsoTypeWriter;
import com.mp4parser.tools.Mp4Arrays;
import com.mp4parser.tools.Mp4Math;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static com.mp4parser.tools.CastUtils.l2i;


/**
 *
 */
public class MultiTrackFragmentedMp4Writer implements StreamingMp4Writer {
    private static final Logger LOG = Logger.getLogger(MultiTrackFragmentedMp4Writer.class.getName());

    private final OutputStream outputStream;
    private StreamingTrack source[];
    private SampleFlagsTrackExtension sampleDependencyTrackExtension;
    private Date creationTime;
    private Map<StreamingTrack, List<StreamingSample>> fragmentBuffers = new HashMap<StreamingTrack, List<StreamingSample>>();
    private long sequenceNumber = 1;
    private long currentFragmentStartTime = 0;
    private long currentTime = 0;


    private ExecutorService es;
    private int maxTimeOuts = 3;
    private int timeOut = 100;
    private boolean closed = false;

    public MultiTrackFragmentedMp4Writer(StreamingTrack[] source, OutputStream outputStream) {
        this.source = source;
        this.outputStream = outputStream;
        this.creationTime = new Date();
        HashSet<Long> trackIds = new HashSet<Long>();
        for (StreamingTrack streamingTrack : source) {
            fragmentBuffers.put(streamingTrack, new ArrayList<StreamingSample>());
            if (streamingTrack.getTrackExtension(TrackIdTrackExtension.class) != null) {
                TrackIdTrackExtension trackIdTrackExtension = streamingTrack.getTrackExtension(TrackIdTrackExtension.class);
                assert trackIdTrackExtension != null;
                if (trackIds.contains(trackIdTrackExtension.getTrackId())) {
                    throw new RuntimeException("There may not be two tracks with the same trackID within one file");
                }
            }
        }
        for (StreamingTrack streamingTrack : source) {
            if (streamingTrack.getTrackExtension(TrackIdTrackExtension.class) == null) {
                ArrayList<Long> ts = new ArrayList<Long>(trackIds);
                Collections.sort(ts);
                TrackIdTrackExtension tiExt = new TrackIdTrackExtension(ts.size() > 0 ? (ts.get(ts.size() - 1) + 1) : 1);
                trackIds.add(tiExt.getTrackId());
                streamingTrack.addTrackExtension(tiExt);
            }
        }

    }

    public void setMaxTimeOuts(int maxTimeOuts) {
        this.maxTimeOuts = maxTimeOuts;
    }

    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    public void close() throws IOException {
        this.closed = true;
        es.shutdown();
        for (StreamingTrack streamingTrack : source) {
            streamingTrack.close();
        }
        try {
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
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
        minorBrands.add("iso6");
        minorBrands.add("avc1");
        return new FileTypeBox("isom", 0, minorBrands);
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
        if ("soun".equals(streamingTrack.getHandler()) || "subt".equals(streamingTrack.getHandler())) {
            // as far as I know there is no audio encoding
            // where the sample are not self contained.
            // same seems to be true for subtitle tracks
            sf.setSampleDependsOn(2);
            sf.setSampleIsDependedOn(2);
        }
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

    class ConsumeSamplesCallable implements Callable<Void> {

        private StreamingTrack streamingTrack;
        int forceEndOfStream = 0;

        public ConsumeSamplesCallable(StreamingTrack streamingTrack) {
            this.streamingTrack = streamingTrack;
        }

        public Void call() throws Exception {
            do {
                try {
                    StreamingSample ss;
                    while ((ss = streamingTrack.getSamples().poll(timeOut, TimeUnit.MILLISECONDS)) != null) {
                        //System.out.println(streamingTrack.getTrackExtension(TrackIdTrackExtension.class).getTrackId() + " Before consume");
                        consumeSample(streamingTrack, ss);
                        //System.out.println(streamingTrack.getTrackExtension(TrackIdTrackExtension.class).getTrackId() + " consumed");
                        forceEndOfStream = 0;
                    }
                    if (streamingTrack.hasMoreSamples() && !closed) {
                        LOG.warning("No Sample acquired. 'poll()' timed out.");
                    }
                    forceEndOfStream++;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (streamingTrack.hasMoreSamples() && forceEndOfStream < maxTimeOuts && !closed);
            return null;
        }
    }

    public void write() throws IOException {
        final WritableByteChannel out = Channels.newChannel(outputStream);

        createFtyp().getBox(out);
        createMoov().getBox(out);
        es = Executors.newFixedThreadPool(source.length);
        for (StreamingTrack streamingTrack : source) {
            es.submit(new ConsumeSamplesCallable(streamingTrack));
        }

        try {
            //System.out.println("-1- es.awaitTermination in MultiTrackFragmentedMp4Writer");
            es.shutdown();
            es.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
            //System.out.println("-2- es.awaitTermination in MultiTrackFragmentedMp4Writer");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void createFragment(StreamingTrack streamingTrack) throws IOException {
        WritableByteChannel out = Channels.newChannel(outputStream);
        createMoof(streamingTrack).getBox(out);
        createMdat(streamingTrack).getBox(out);
        fragmentBuffers.get(streamingTrack).clear();
    }

    private synchronized void consumeSample(StreamingTrack streamingTrack, StreamingSample sample) throws IOException {
        //System.err.println("Consuming " + streamingTrack.getTrackExtension(TrackIdTrackExtension.class).getTrackId() + " " + ss.getDuration());
        SampleFlagsSampleExtension sampleDependencySampleExtension = sample.getSampleExtension(SampleFlagsSampleExtension.class);
        currentTime += sample.getDuration();
        // 3 seconds = 3 * source.getTimescale()
        fragmentBuffers.get(streamingTrack).add(sample);
        if (currentTime > currentFragmentStartTime + 3 * streamingTrack.getTimescale() &&
                fragmentBuffers.get(streamingTrack).size() > 0 &&
                (sampleDependencyTrackExtension == null ||
                        sampleDependencySampleExtension == null ||
                        sampleDependencySampleExtension.isSyncSample())) {
            createFragment(streamingTrack);
            currentFragmentStartTime = currentTime;

        }
    }

    private ParsableBox createMoof(StreamingTrack streamingTrack) {
        MovieFragmentBox moof = new MovieFragmentBox();
        createMfhd(sequenceNumber, moof);
        createTraf(streamingTrack, moof);

        TrackRunBox firstTrun = moof.getTrackRunBoxes().get(0);
        firstTrun.setDataOffset(1); // dummy to make size correct
        firstTrun.setDataOffset((int) (8 + moof.getSize())); // mdat header + moof size

        sequenceNumber++;
        return moof;

    }

    protected void createTfhd(StreamingTrack streamingTrack, TrackFragmentBox parent) {
        TrackFragmentHeaderBox tfhd = new TrackFragmentHeaderBox();
        SampleFlags sf = new SampleFlags();

        tfhd.setDefaultSampleFlags(sf);
        tfhd.setBaseDataOffset(-1);
        tfhd.setTrackId(streamingTrack.getTrackExtension(TrackIdTrackExtension.class).getTrackId());
        tfhd.setDefaultBaseIsMoof(true);
        parent.addBox(tfhd);
    }

    protected void createTfdt(TrackFragmentBox parent) {
        TrackFragmentBaseMediaDecodeTimeBox tfdt = new TrackFragmentBaseMediaDecodeTimeBox();
        tfdt.setVersion(1);
        tfdt.setBaseMediaDecodeTime(currentFragmentStartTime);
        parent.addBox(tfdt);
    }

    protected void createTrun(StreamingTrack streamingTrack, TrackFragmentBox parent) {
        TrackRunBox trun = new TrackRunBox();
        trun.setVersion(1);

        trun.setSampleDurationPresent(true);
        trun.setSampleSizePresent(true);
        List<TrackRunBox.Entry> entries = new ArrayList<TrackRunBox.Entry>(fragmentBuffers.size());


        trun.setSampleCompositionTimeOffsetPresent(streamingTrack.getTrackExtension(CompositionTimeTrackExtension.class) != null);

        boolean sampleFlagsRequired = streamingTrack.getTrackExtension(SampleFlagsTrackExtension.class) != null;

        trun.setSampleFlagsPresent(sampleFlagsRequired);

        for (StreamingSample streamingSample : fragmentBuffers.get(streamingTrack)) {
            TrackRunBox.Entry entry = new TrackRunBox.Entry();
            entry.setSampleSize(streamingSample.getContent().remaining());
            if (sampleFlagsRequired) {
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


    private void createTraf(StreamingTrack streamingTrack, MovieFragmentBox moof) {
        TrackFragmentBox traf = new TrackFragmentBox();
        moof.addBox(traf);
        createTfhd(streamingTrack, traf);
        createTfdt(traf);
        createTrun(streamingTrack, traf);

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


    private void createMfhd(long sequenceNumber, MovieFragmentBox moof) {
        MovieFragmentHeaderBox mfhd = new MovieFragmentHeaderBox();
        mfhd.setSequenceNumber(sequenceNumber);
        moof.addBox(mfhd);
    }

    private Box createMdat(final StreamingTrack streamingTrack) {
        final List<StreamingSample> samples = new ArrayList<StreamingSample>(fragmentBuffers.get(streamingTrack));
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
