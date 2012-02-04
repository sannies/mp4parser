package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.boxes.adobe.ActionMessageFormat0SampleEntryBox;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 1/31/12
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Amf0Track extends AbstractTrack {
    Map<Long, byte[]> rawSamples = new HashMap<Long, byte[]>();
    private TrackMetaData trackMetaData = new TrackMetaData();


    /**
     * Creates a new AMF0 track from
     * @param rawSamples
     */
    public Amf0Track(Map<Long, byte[]> rawSamples) {
        this.rawSamples = rawSamples;
        trackMetaData.setCreationTime(new Date());
        trackMetaData.setModificationTime(new Date());
        trackMetaData.setTimescale(1000); // Text tracks use millieseconds
        trackMetaData.setLanguage("eng");
    }

    public List<IsoBufferWrapper> getSamples() {
        List<Long> keys = new LinkedList<Long>(rawSamples.keySet());
        List<IsoBufferWrapper> ibws = new LinkedList<IsoBufferWrapper>();
        Collections.sort(keys);
        for (Long key : keys) {
            ibws.add(new IsoBufferWrapperImpl(rawSamples.get(key)));
        }
        return ibws;
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        SampleDescriptionBox stsd = new SampleDescriptionBox();
        ActionMessageFormat0SampleEntryBox amf0 = new ActionMessageFormat0SampleEntryBox();
        amf0.setDataReferenceIndex(1);
        stsd.addBox(amf0);
        return stsd;
    }

    public List<TimeToSampleBox.Entry> getDecodingTimeEntries() {
        LinkedList<TimeToSampleBox.Entry> timesToSample = new LinkedList<TimeToSampleBox.Entry>();
        LinkedList<Long> keys = new LinkedList<Long>(rawSamples.keySet());
        Collections.sort(keys);
        long lastTimeStamp = 0;
        for (Long key : keys) {
            long delta = key - lastTimeStamp;
            if (timesToSample.size() > 0 && timesToSample.peek().getDelta() == delta) {
                timesToSample.peek().setCount(timesToSample.peek().getCount() + 1);
            } else {
                timesToSample.add(new TimeToSampleBox.Entry(1, delta));
            }
            lastTimeStamp = key;
        }
        return timesToSample;
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        // AMF0 tracks do not have Composition Time
        return null;
    }

    public long[] getSyncSamples() {
        // AMF0 tracks do not have Sync Samples
        return null;
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        // AMF0 tracks do not have Sample Dependencies
        return null;
    }

    public TrackMetaData getTrackMetaData() {
        return trackMetaData;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Type getType() {
        return Type.AMF0;
    }
}
