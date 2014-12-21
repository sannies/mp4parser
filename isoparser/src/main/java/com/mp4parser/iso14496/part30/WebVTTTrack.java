package com.mp4parser.iso14496.part30;

import com.coremedia.iso.Utf8;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.TrackMetaData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Created by sannies on 05.12.2014.
 */
public class WebVTTTrack extends AbstractTrack {
    String[] subs;

    List<Sample> samples = new ArrayList<Sample>();
    WebVTTSampleEntry sampleEntry;

    public WebVTTTrack(DataSource dataSource) throws IOException {
        super(dataSource.toString());
        sampleEntry = new WebVTTSampleEntry();
        sampleEntry.addBox(new WebVTTConfigurationBox());
        sampleEntry.addBox(new WebVTTSourceLabelBox());

        ByteBuffer bb = dataSource.map(0, l2i(dataSource.size()));
        byte[] content = new byte[l2i(dataSource.size())];
        bb.get(content);
        subs = Utf8.convert(content).split("\\r?\\n");

        //Header
        int i;
        String config = "";
        for (i = 0; i < subs.length; i++) {
            config += subs[i] + "\n";
            if (subs[i + 1].isEmpty() && subs[i + 2].isEmpty()) {
                break;
            }
        }
        // ffw to actual content
        for (;i < subs.length; i++) {
            if (!subs[i].isEmpty()) {
                break;
            }
        }


    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return null;
    }

    public long[] getSampleDurations() {
        return new long[0];
    }

    public TrackMetaData getTrackMetaData() {
        return null;
    }

    public String getHandler() {
        return null;
    }

    public List<Sample> getSamples() {
        return null;
    }

    public void close() throws IOException {

    }
}
