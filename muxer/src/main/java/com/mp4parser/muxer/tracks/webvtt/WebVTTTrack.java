package com.mp4parser.muxer.tracks.webvtt;

import com.mp4parser.boxes.iso14496.part30.WebVTTConfigurationBox;
import com.mp4parser.boxes.iso14496.part30.WebVTTSampleEntry;
import com.mp4parser.boxes.iso14496.part30.WebVTTSourceLabelBox;
import com.mp4parser.tools.Utf8;
import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import com.mp4parser.muxer.DataSource;
import com.mp4parser.muxer.AbstractTrack;
import com.mp4parser.muxer.Sample;
import com.mp4parser.muxer.TrackMetaData;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.mp4parser.tools.CastUtils.l2i;

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
