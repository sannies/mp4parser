package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.boxes.AbstractMediaHeaderBox;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.HintMediaHeaderBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.NullMediaHeaderBox;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.SoundMediaHeaderBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.coremedia.iso.boxes.VideoMediaHeaderBox;
import com.coremedia.iso.boxes.mdat.SampleList;

import java.util.List;

/**
 * Represents a single track of an MP4 file.
 */
public class Mp4TrackImpl extends AbstractTrack {

    private SampleList samples;
    private SampleDescriptionBox sampleDescriptionBox;
    private List<TimeToSampleBox.Entry> decodingTimeEntries;
    private List<CompositionTimeToSample.Entry> compositionTimeEntries;
    private long[] syncSamples;
    private List<SampleDependencyTypeBox.Entry> sampleDependencies;
    private TrackMetaData trackMetaData = new TrackMetaData();
    private Type type;


    public Mp4TrackImpl(TrackBox trackBox) {
        samples = new SampleList(trackBox);
        SampleTableBox stbl = trackBox.getMediaBox().getMediaInformationBox().getSampleTableBox();
        AbstractMediaHeaderBox mihd = trackBox.getMediaBox().getMediaInformationBox().getMediaHeaderBox();
        if (mihd instanceof VideoMediaHeaderBox) {
            type = Type.VIDEO;
        } else if (mihd instanceof SoundMediaHeaderBox) {
            type = Type.SOUND;
        } else if (mihd instanceof HintMediaHeaderBox) {
            type = Type.HINT;
        } else if (mihd instanceof NullMediaHeaderBox) {
            type = Type.NULL;
        } else {
            type = Type.UNKNOWN;
        }

        sampleDescriptionBox = stbl.getSampleDescriptionBox();
        decodingTimeEntries = stbl.getTimeToSampleBox().getEntries();
        if (stbl.getCompositionTimeToSample() != null) {
            compositionTimeEntries = stbl.getCompositionTimeToSample().getEntries();
        }
        if (stbl.getSyncSampleBox() != null) {
            syncSamples = stbl.getSyncSampleBox().getSampleNumber();
        }
        if (stbl.getSampleDependencyTypeBox() != null) {
            sampleDependencies = stbl.getSampleDependencyTypeBox().getEntries();
        }
        MediaHeaderBox mdhd = trackBox.getMediaBox().getMediaHeaderBox();
        TrackHeaderBox tkhd = trackBox.getTrackHeaderBox();

        setEnabled(tkhd.isEnabled());
        setInMovie(tkhd.isInMovie());
        setInPoster(tkhd.isInPoster());
        setInPreview(tkhd.isInPreview());

        trackMetaData.setTrackId(tkhd.getTrackId());
        trackMetaData.setCreationTime(DateHelper.convert(mdhd.getCreationTime()));
        trackMetaData.setLanguage(mdhd.getLanguage());
        System.err.println(mdhd.getModificationTime());
        System.err.println(DateHelper.convert(mdhd.getModificationTime()));
        System.err.println(DateHelper.convert(DateHelper.convert(mdhd.getModificationTime())));
        System.err.println(DateHelper.convert(DateHelper.convert(DateHelper.convert(mdhd.getModificationTime()))));

        trackMetaData.setModificationTime(DateHelper.convert(mdhd.getModificationTime()));
        trackMetaData.setTimescale(mdhd.getTimescale());
        trackMetaData.setHeight(tkhd.getHeight());
        trackMetaData.setWidth(tkhd.getWidth());
        trackMetaData.setLayer(tkhd.getLayer());
    }

    public SampleList getSamples() {
        return samples;
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return sampleDescriptionBox;
    }

    public List<TimeToSampleBox.Entry> getDecodingTimeEntries() {
        return decodingTimeEntries;
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return compositionTimeEntries;
    }

    public long[] getSyncSamples() {
        return syncSamples;
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return sampleDependencies;
    }

    public TrackMetaData getTrackMetaData() {
        return trackMetaData;
    }

    public Type getType() {
        return type;
    }

}
