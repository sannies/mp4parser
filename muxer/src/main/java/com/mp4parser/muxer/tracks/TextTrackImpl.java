/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mp4parser.muxer.tracks;

import com.mp4parser.boxes.iso14496.part12.SubSampleInformationBox;
import com.mp4parser.boxes.sampleentry.TextSampleEntry;
import com.mp4parser.muxer.AbstractTrack;
import com.mp4parser.muxer.Sample;
import com.mp4parser.muxer.SampleImpl;
import com.mp4parser.muxer.TrackMetaData;
import com.mp4parser.boxes.threegpp.ts26245.FontTableBox;
import com.mp4parser.boxes.iso14496.part12.CompositionTimeToSample;
import com.mp4parser.boxes.iso14496.part12.SampleDependencyTypeBox;
import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;

/**
 *
 */
public class TextTrackImpl extends AbstractTrack {
    TrackMetaData trackMetaData = new TrackMetaData();
    SampleDescriptionBox sampleDescriptionBox;
    List<Line> subs = new LinkedList<Line>();

    public List<Line> getSubs() {
        return subs;
    }

    public TextTrackImpl() {
        super("subtiles");
        sampleDescriptionBox = new SampleDescriptionBox();
        TextSampleEntry tx3g = new TextSampleEntry("tx3g");
        tx3g.setDataReferenceIndex(1);
        tx3g.setStyleRecord(new TextSampleEntry.StyleRecord());
        tx3g.setBoxRecord(new TextSampleEntry.BoxRecord());
        sampleDescriptionBox.addBox(tx3g);

        FontTableBox ftab = new FontTableBox();
        ftab.setEntries(Collections.singletonList(new FontTableBox.FontRecord(1, "Serif")));

        tx3g.addBox(ftab);


        trackMetaData.setCreationTime(new Date());
        trackMetaData.setModificationTime(new Date());
        trackMetaData.setTimescale(1000); // Text tracks use millieseconds


    }

    public void close() throws IOException {
        // nothing to close
    }

    public List<Sample> getSamples() {
        List<Sample> samples = new LinkedList<Sample>();
        long lastEnd = 0;
        for (Line sub : subs) {
            long silentTime = sub.from - lastEnd;
            if (silentTime > 0) {
                samples.add(new SampleImpl(ByteBuffer.wrap(new byte[]{0, 0})));
            } else if (silentTime < 0) {
                throw new Error("Subtitle display times may not intersect");
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            try {
                dos.writeShort(sub.text.getBytes("UTF-8").length);
                dos.write(sub.text.getBytes("UTF-8"));
                dos.close();
            } catch (IOException e) {
                throw new Error("VM is broken. Does not support UTF-8");
            }
            samples.add(new SampleImpl(ByteBuffer.wrap(baos.toByteArray())));
            lastEnd = sub.to;
        }
        return samples;
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return sampleDescriptionBox;
    }

    public long[] getSampleDurations() {
        List<Long> decTimes = new ArrayList<Long>();

        long lastEnd = 0;
        for (Line sub : subs) {
            long silentTime = sub.from - lastEnd;
            if (silentTime > 0) {

                decTimes.add(silentTime);
            } else if (silentTime < 0) {
                throw new Error("Subtitle display times may not intersect");
            }
            decTimes.add( sub.to - sub.from);
            lastEnd = sub.to;
        }
        long[] decTimesArray = new long[decTimes.size()];
        int index = 0;
        for (Long decTime : decTimes) {
            decTimesArray[index++] = decTime;
        }
        return decTimesArray;
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return null;
    }

    public long[] getSyncSamples() {
        return null;
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return null;
    }

    public TrackMetaData getTrackMetaData() {
        return trackMetaData;
    }

    public String getHandler() {
        return "sbtl";
    }


    public static class Line {
        long from;
        long to;
        String text;


        public Line(long from, long to, String text) {
            this.from = from;
            this.to = to;
            this.text = text;
        }

        public long getFrom() {
            return from;
        }

        public String getText() {
            return text;
        }

        public long getTo() {
            return to;
        }
    }


    public SubSampleInformationBox getSubsampleInformationBox() {
        return null;
    }
}
