package org.mp4parser.streaming.output.mp4;

import org.mp4parser.Box;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.extensions.DimensionTrackExtension;
import org.mp4parser.streaming.extensions.TrackIdTrackExtension;

import java.util.LinkedList;
import java.util.List;

public abstract class DefaultBoxes {

    public Box createFtyp() {
        List<String> minorBrands = new LinkedList<String>();
        minorBrands.add("isom");
        minorBrands.add("iso2");
        minorBrands.add("avc1");
        minorBrands.add("iso6");
        minorBrands.add("mp41");
        return new FileTypeBox("isom", 512, minorBrands);
    }

    protected Box createMdiaHdlr(StreamingTrack streamingTrack) {
        HandlerBox hdlr = new HandlerBox();
        hdlr.setHandlerType(streamingTrack.getHandler());
        return hdlr;
    }

    protected Box createMdia(StreamingTrack streamingTrack) {
        MediaBox mdia = new MediaBox();
        mdia.addBox(createMdhd(streamingTrack));
        mdia.addBox(createMdiaHdlr(streamingTrack));
        mdia.addBox(createMinf(streamingTrack));
        return mdia;
    }

    abstract protected Box createMdhd(StreamingTrack streamingTrack);

    abstract protected Box createMvhd();

    protected Box createMinf(StreamingTrack streamingTrack) {
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

    protected Box createStbl(StreamingTrack streamingTrack) {
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

    protected Box createTrak(StreamingTrack streamingTrack) {
        TrackBox trackBox = new TrackBox();
        trackBox.addBox(createTkhd(streamingTrack));
        trackBox.addBox(createMdia(streamingTrack));
        return trackBox;
    }

    protected Box createTkhd(StreamingTrack streamingTrack) {
        TrackHeaderBox tkhd = new TrackHeaderBox();
        tkhd.setTrackId(streamingTrack.getTrackExtension(TrackIdTrackExtension.class).getTrackId());
        DimensionTrackExtension dte = streamingTrack.getTrackExtension(DimensionTrackExtension.class);
        if (dte != null) {
            tkhd.setHeight(dte.getHeight());
            tkhd.setWidth(dte.getWidth());
        }
        return tkhd;
    }

}
