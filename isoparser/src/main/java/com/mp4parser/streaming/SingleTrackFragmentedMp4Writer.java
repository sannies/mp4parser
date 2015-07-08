package com.mp4parser.streaming;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox;
import com.coremedia.iso.boxes.fragment.MovieExtendsHeaderBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentHeaderBox;
import com.coremedia.iso.boxes.fragment.SampleFlags;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBaseMediaDecodeTimeBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentHeaderBox;
import com.coremedia.iso.boxes.fragment.TrackRunBox;
import com.mp4parser.streaming.extensions.CencEncryptTrackExtension;
import com.mp4parser.streaming.extensions.CompositionTimeSampleExtension;
import com.mp4parser.streaming.extensions.CompositionTimeTrackExtension;
import com.mp4parser.streaming.extensions.SampleFlagsSampleExtension;
import com.mp4parser.streaming.extensions.SampleFlagsTrackExtension;
import com.mp4parser.streaming.extensions.TrackIdTrackExtension;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mp4parser.streaming.StreamingSampleHelper.getSampleExtension;
import static com.mp4parser.streaming.StreamingTrackHelper.getTrackExtension;
import static com.mp4parser.streaming.StreamingTrackHelper.hasTrackExtension;

/**
 *
 */
public class SingleTrackFragmentedMp4Writer implements StreamingMp4Writer {
    private final OutputStream outputStream;
    StreamingTrack source;
    CompositionTimeTrackExtension compositionTimeTrackExtension;
    SampleFlagsTrackExtension sampleDependencyTrackExtension;

    Date creationTime;

    List<StreamingSample> fragment = new ArrayList<StreamingSample>();
    private long sequenceNumber;
    private long currentFragmentStartTime = 0;

    public SingleTrackFragmentedMp4Writer(StreamingTrack source, OutputStream outputStream) {
        this.source = source;
        this.outputStream = outputStream;
        this.creationTime = new Date();
        for (TrackExtension trackExtension : source.getExtensions()) {
            if (trackExtension instanceof CompositionTimeTrackExtension) {
                compositionTimeTrackExtension = (CompositionTimeTrackExtension) trackExtension;
            } else if (trackExtension instanceof SampleFlagsTrackExtension) {
                sampleDependencyTrackExtension = (SampleFlagsTrackExtension) trackExtension;
            }
        }
    }

    public void close() {

    }


    protected Box createMvhd() {
        MovieHeaderBox mvhd = new MovieHeaderBox();
        mvhd.setVersion(1);
        mvhd.setCreationTime(creationTime);
        mvhd.setModificationTime(creationTime);
        mvhd.setDuration(0);//no duration in moov for fragmented movies
        long movieTimeScale = source.getTimescale();
        mvhd.setTimescale(movieTimeScale);
        // find the next available trackId
        mvhd.setNextTrackId(2);
        return mvhd;
    }

    protected Box createMdiaHdlr() {
        HandlerBox hdlr = new HandlerBox();
        hdlr.setHandlerType(source.getHandler());
        return hdlr;
    }

    protected Box createMdhd() {
        MediaHeaderBox mdhd = new MediaHeaderBox();
        mdhd.setCreationTime(creationTime);
        mdhd.setModificationTime(creationTime);
        mdhd.setDuration(0);//no duration in moov for fragmented movies
        mdhd.setTimescale(source.getTimescale());
        mdhd.setLanguage(source.getLanguage());
        return mdhd;
    }


    protected Box createMdia() {
        MediaBox mdia = new MediaBox();
        mdia.addBox(createMdhd());
        mdia.addBox(createMdiaHdlr());
        mdia.addBox(createMinf());
        return mdia;
    }

    protected Box createMinf() {
        MediaInformationBox minf = new MediaInformationBox();
        if (source.getHandler().equals("vide")) {
            minf.addBox(new VideoMediaHeaderBox());
        } else if (source.getHandler().equals("soun")) {
            minf.addBox(new SoundMediaHeaderBox());
        } else if (source.getHandler().equals("text")) {
            minf.addBox(new NullMediaHeaderBox());
        } else if (source.getHandler().equals("subt")) {
            minf.addBox(new SubtitleMediaHeaderBox());
        } else if (source.getHandler().equals("hint")) {
            minf.addBox(new HintMediaHeaderBox());
        } else if (source.getHandler().equals("sbtl")) {
            minf.addBox(new NullMediaHeaderBox());
        }
        minf.addBox(createDinf());
        minf.addBox(createStbl());
        return minf;
    }

    protected Box createStbl() {
        SampleTableBox stbl = new SampleTableBox();

        stbl.addBox(source.getSampleDescriptionBox());
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

    protected Box createTrak() {
        TrackBox trackBox = new TrackBox();
        trackBox.addBox(source.getTrackHeaderBox());
        trackBox.addBox(createMdia());
        return trackBox;
    }


    public Box createFtyp() {
        List<String> minorBrands = new LinkedList<String>();
        minorBrands.add("isom");
        minorBrands.add("iso6");
        minorBrands.add("avc1");
        return new FileTypeBox("isom", 0, minorBrands);
    }

    protected Box createMvex() {
        MovieExtendsBox mvex = new MovieExtendsBox();
        final MovieExtendsHeaderBox mved = new MovieExtendsHeaderBox();
        mved.setVersion(1);

        mved.setFragmentDuration(0);

        mvex.addBox(mved);

        mvex.addBox(createTrex());
        return mvex;
    }

    protected Box createTrex() {
        TrackExtendsBox trex = new TrackExtendsBox();
        trex.setTrackId(1);
        trex.setDefaultSampleDescriptionIndex(1);
        trex.setDefaultSampleDuration(0);
        trex.setDefaultSampleSize(0);
        SampleFlags sf = new SampleFlags();
        if ("soun".equals(source.getHandler()) || "subt".equals(source.getHandler())) {
            // as far as I know there is no audio encoding
            // where the sample are not self contained.
            // same seems to be true for subtitle tracks
            sf.setSampleDependsOn(2);
            sf.setSampleIsDependedOn(2);
        }
        trex.setDefaultSampleFlags(sf);
        return trex;
    }


    protected Box createMoov() {
        MovieBox movieBox = new MovieBox();

        movieBox.addBox(createMvhd());

        movieBox.addBox(createTrak());
        movieBox.addBox(createMvex());

        // metadata here
        return movieBox;
    }

    public void write() throws IOException {
        WritableByteChannel out = Channels.newChannel(outputStream);

        createFtyp().getBox(out);
        createMoov().getBox(out);


        do {
            try {
                StreamingSample ss;
                while ((ss = source.getSamples().poll(100, TimeUnit.MILLISECONDS)) != null) {
                    consumeSample(ss, out);
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } while (source.hasMoreSamples());
    }


    private void consumeSample(StreamingSample ss, WritableByteChannel out) throws IOException {
        SampleFlagsSampleExtension sampleDependencySampleExtension = null;
        CompositionTimeSampleExtension compositionTimeSampleExtension = null;
        for (SampleExtension sampleExtension : ss.getExtensions()) {
            if (sampleExtension instanceof SampleFlagsSampleExtension) {
                sampleDependencySampleExtension = (SampleFlagsSampleExtension) sampleExtension;
            } else if (sampleExtension instanceof CompositionTimeSampleExtension) {
                compositionTimeSampleExtension = (CompositionTimeSampleExtension) sampleExtension;
            }
        }

        // 3 seconds = 3 * source.getTimescale()
        if (ss.getDuration() > currentFragmentStartTime + 3 * source.getTimescale() &&
                fragment.size() > 0 &&
                (sampleDependencyTrackExtension == null ||
                        sampleDependencySampleExtension == null ||
                        sampleDependencySampleExtension.isSyncSample())) {
            createMoof().getBox(out);
            createMdat().getBox(out);


        }
    }

    private Box createMoof() {
        MovieFragmentBox moof = new MovieFragmentBox();
        createMfhd(sequenceNumber, moof);
        createTraf(sequenceNumber, moof);

        TrackRunBox firstTrun = moof.getTrackRunBoxes().get(0);
        firstTrun.setDataOffset(1); // dummy to make size correct
        firstTrun.setDataOffset((int) (8 + moof.getSize())); // mdat header + moof size

        return moof;

    }

    protected void createTfhd(TrackFragmentBox parent) {
        TrackFragmentHeaderBox tfhd = new TrackFragmentHeaderBox();
        SampleFlags sf = new SampleFlags();

        tfhd.setDefaultSampleFlags(sf);
        tfhd.setBaseDataOffset(-1);
        tfhd.setTrackId(hasTrackExtension(source, TrackIdTrackExtension.class) ? getTrackExtension(source, TrackIdTrackExtension.class).getTrackId() : 1);
        tfhd.setDefaultBaseIsMoof(true);
        parent.addBox(tfhd);
    }

    protected void createTfdt(TrackFragmentBox parent) {
        TrackFragmentBaseMediaDecodeTimeBox tfdt = new TrackFragmentBaseMediaDecodeTimeBox();
        tfdt.setVersion(1);
        tfdt.setBaseMediaDecodeTime(currentFragmentStartTime);
        parent.addBox(tfdt);
    }

    protected void createTrun( TrackFragmentBox parent) {
        TrackRunBox trun = new TrackRunBox();
        trun.setVersion(1);

        trun.setSampleDurationPresent(true);
        trun.setSampleSizePresent(true);
        List<TrackRunBox.Entry> entries = new ArrayList<TrackRunBox.Entry>(fragment.size());




        trun.setSampleCompositionTimeOffsetPresent(hasTrackExtension(source, CompositionTimeTrackExtension.class));

        boolean sampleFlagsRequired = (hasTrackExtension(source, SampleFlagsTrackExtension.class));

        trun.setSampleFlagsPresent(sampleFlagsRequired);

        for (StreamingSample streamingSample: fragment) {
            TrackRunBox.Entry entry = new TrackRunBox.Entry();
            entry.setSampleSize(streamingSample.getContent().remaining());
            if (sampleFlagsRequired) {
                SampleFlagsSampleExtension sampleFlagsSampleExtension =
                        getSampleExtension(streamingSample, SampleFlagsSampleExtension.class);
                assert sampleFlagsSampleExtension != null:"SampleDependencySampleExtension missing even though SampleDependencyTrackExtension was present";
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
                CompositionTimeSampleExtension compositionTimeSampleExtension =
                        getSampleExtension(streamingSample, CompositionTimeSampleExtension.class);
                assert compositionTimeSampleExtension != null:"CompositionTimeSampleExtension missing even though CompositionTimeTrackExtension was present";
                entry.setSampleCompositionTimeOffset(compositionTimeSampleExtension.getCompositionTimeOffset());
            }

            entries.add(entry);
        }

        trun.setEntries(entries);

        parent.addBox(trun);
    }

    private void createTraf(long sequenceNumber, MovieFragmentBox moof) {
        TrackFragmentBox traf = new TrackFragmentBox();
        moof.addBox(traf);
        createTfhd(traf);
        createTfdt(traf);
        createTrun( traf);

        if (hasTrackExtension(source, CencEncryptTrackExtension.class)) {
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

    private Box createMdat() {
        return new WriteOnlyBox("mdat") {
            public long getSize() {
                long l = 8;
                for (StreamingSample streamingSample : fragment) {
                    l += streamingSample.getContent().remaining();
                }
                return l;
            }

            public void getBox(WritableByteChannel writableByteChannel) throws IOException {
                ArrayList<ByteBuffer> sampleContents = new ArrayList<ByteBuffer>();
                long l = 8;
                for (StreamingSample streamingSample : fragment) {
                    ByteBuffer sampleContent = streamingSample.getContent();
                    sampleContents.add(sampleContent);
                    l += sampleContent.remaining();
                }
                ByteBuffer bb = ByteBuffer.allocate(8);
                IsoTypeWriter.writeUInt32(bb, l);
                bb.put(IsoFile.fourCCtoBytes(getType()));

                for (ByteBuffer sampleContent : sampleContents) {
                    writableByteChannel.write(sampleContent);
                }
            }

        };
    }
}
