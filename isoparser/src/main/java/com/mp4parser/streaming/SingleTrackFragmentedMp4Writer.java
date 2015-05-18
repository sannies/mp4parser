package com.mp4parser.streaming;

import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox;
import com.coremedia.iso.boxes.fragment.MovieExtendsHeaderBox;
import com.coremedia.iso.boxes.fragment.SampleFlags;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class SingleTrackFragmentedMp4Writer implements StreamingMp4Writer {
    private final OutputStream outputStream;
    StreamingSampleSource source;
    Date creationTime;

    private long currentFragmentStartTime = 0;

    public SingleTrackFragmentedMp4Writer(StreamingSampleSource source, OutputStream outputStream) {
        this.source = source;
        this.outputStream = outputStream;
        this.creationTime = new Date();
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
        minorBrands.add("iso5");
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


    private void consumeSample(StreamingSample ss, WritableByteChannel out) {
        if (ss.getPresentationTime() > currentFragmentStartTime + 3 * source.getTimescale() &&
                (!source.isSyncSampleAware() || (ss.getSampleDependency() & 0x30) == 0x20)) { // I-frame!

        }
    }
}
