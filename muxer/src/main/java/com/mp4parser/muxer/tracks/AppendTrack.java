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
import com.mp4parser.boxes.sampleentry.AudioSampleEntry;
import com.mp4parser.boxes.sampleentry.SampleEntry;
import com.mp4parser.boxes.sampleentry.VisualSampleEntry;
import com.mp4parser.muxer.AbstractTrack;
import com.mp4parser.muxer.Sample;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.TrackMetaData;
import com.mp4parser.boxes.iso14496.part14.AbstractDescriptorBox;
import com.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import com.mp4parser.boxes.iso14496.part1.objectdescriptors.BaseDescriptor;
import com.mp4parser.boxes.iso14496.part1.objectdescriptors.DecoderConfigDescriptor;
import com.mp4parser.boxes.iso14496.part1.objectdescriptors.ESDescriptor;
import com.mp4parser.support.Logger;
import com.mp4parser.Box;
import com.mp4parser.ParsableBox;
import com.mp4parser.boxes.iso14496.part12.CompositionTimeToSample;
import com.mp4parser.boxes.iso14496.part12.SampleDependencyTypeBox;
import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.*;

/**
 * Appends two or more <code>Tracks</code> of the same type. No only that the type must be equal
 * also the decoder settings must be the same.
 */
public class AppendTrack extends AbstractTrack {
    private static Logger LOG = Logger.getLogger(AppendTrack.class);
    Track[] tracks;
    SampleDescriptionBox stsd;

    public static String appendTracknames (Track... tracks) {
        String name = "";
        for (Track track : tracks) {
            name += track.getName() + " + ";
        }
        return name.substring(0, name.length() -3);
    }

    public AppendTrack(Track... tracks) throws IOException {
        super(appendTracknames(tracks));
        this.tracks = tracks;

        for (Track track : tracks) {

            if (stsd == null) {
                stsd = new SampleDescriptionBox();
                stsd.addBox(track.getSampleDescriptionBox().getBoxes(SampleEntry.class).get(0));
            } else {
                stsd = mergeStsds(stsd, track.getSampleDescriptionBox());

            }
        }
    }

    public void close() throws IOException {
        for (Track track : tracks) {
            track.close();
        }
    }

    private SampleDescriptionBox mergeStsds(SampleDescriptionBox stsd1, SampleDescriptionBox stsd2) throws IOException {
        ByteArrayOutputStream curBaos = new ByteArrayOutputStream();
        ByteArrayOutputStream refBaos = new ByteArrayOutputStream();
        try {
            stsd1.getBox(Channels.newChannel(curBaos));
            stsd2.getBox(Channels.newChannel(refBaos));
        } catch (IOException e) {
            LOG.logError(e.getMessage());
            return null;
        }
        byte[] cur = curBaos.toByteArray();
        byte[] ref = refBaos.toByteArray();

        if (!Arrays.equals(ref, cur)) {
            SampleEntry se = mergeSampleEntry(stsd1.getBoxes(SampleEntry.class).get(0), stsd2.getBoxes(SampleEntry.class).get(0));
            if (se != null) {
                stsd1.setBoxes(Collections.<ParsableBox>singletonList(se));
            } else {
                throw new IOException("Cannot merge " + stsd1.getBoxes(SampleEntry.class).get(0) + " and " + stsd2.getBoxes(SampleEntry.class).get(0));
            }
        }
        return stsd1;
    }

    private SampleEntry mergeSampleEntry(SampleEntry se1, SampleEntry se2) {
        if (!se1.getType().equals(se2.getType())) {
            return null;
        } else if (se1 instanceof VisualSampleEntry && se2 instanceof VisualSampleEntry) {
            return mergeVisualSampleEntry((VisualSampleEntry) se1, (VisualSampleEntry) se2);
        } else if (se1 instanceof AudioSampleEntry && se2 instanceof AudioSampleEntry) {
            return mergeAudioSampleEntries((AudioSampleEntry) se1, (AudioSampleEntry) se2);
        } else {
            return null;
        }
    }

    private VisualSampleEntry mergeVisualSampleEntry(VisualSampleEntry vse1, VisualSampleEntry vse2) {
        VisualSampleEntry vse = new VisualSampleEntry();
        if (vse1.getHorizresolution() == vse2.getHorizresolution()) {
            vse.setHorizresolution(vse1.getHorizresolution());
        } else {
            LOG.logError("Horizontal Resolution differs");
            return null;
        }
        vse.setCompressorname(vse1.getCompressorname()); // ignore if they differ
        if (vse1.getDepth() == vse2.getDepth()) {
            vse.setDepth(vse1.getDepth());
        } else {
            LOG.logError("Depth differs");
            return null;
        }

        if (vse1.getFrameCount() == vse2.getFrameCount()) {
            vse.setFrameCount(vse1.getFrameCount());
        } else {
            LOG.logError("frame count differs");
            return null;
        }

        if (vse1.getHeight() == vse2.getHeight()) {
            vse.setHeight(vse1.getHeight());
        } else {
            LOG.logError("height differs");
            return null;
        }
        if (vse1.getWidth() == vse2.getWidth()) {
            vse.setWidth(vse1.getWidth());
        } else {
            LOG.logError("width differs");
            return null;
        }

        if (vse1.getVertresolution() == vse2.getVertresolution()) {
            vse.setVertresolution(vse1.getVertresolution());
        } else {
            LOG.logError("vert resolution differs");
            return null;
        }

        if (vse1.getHorizresolution() == vse2.getHorizresolution()) {
            vse.setHorizresolution(vse1.getHorizresolution());
        } else {
            LOG.logError("horizontal resolution differs");
            return null;
        }

        if (vse1.getBoxes().size() == vse2.getBoxes().size()) {
            Iterator<Box> bxs1 = vse1.getBoxes().iterator();
            Iterator<Box> bxs2 = vse2.getBoxes().iterator();
            while (bxs1.hasNext()) {
                Box cur1 = bxs1.next();
                Box cur2 = bxs2.next();
                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                try {
                    cur1.getBox(Channels.newChannel(baos1));
                    cur2.getBox(Channels.newChannel(baos2));
                } catch (IOException e) {
                    LOG.logWarn(e.getMessage());
                    return null;
                }
                if (Arrays.equals(baos1.toByteArray(), baos2.toByteArray())) {
                    vse.addBox(cur1);
                } else {
                    if (cur1 instanceof AbstractDescriptorBox && cur2 instanceof AbstractDescriptorBox) {
                        BaseDescriptor esd = mergeDescriptors(((AbstractDescriptorBox) cur1).getDescriptor(), ((AbstractDescriptorBox) cur2).getDescriptor());
                        ((AbstractDescriptorBox) cur1).setDescriptor(esd);
                        vse.addBox(cur1);
                    }
                }
            }
        }
        return vse;
    }

    private AudioSampleEntry mergeAudioSampleEntries(AudioSampleEntry ase1, AudioSampleEntry ase2) {
        AudioSampleEntry ase = new AudioSampleEntry(ase2.getType());
        if (ase1.getBytesPerFrame() == ase2.getBytesPerFrame()) {
            ase.setBytesPerFrame(ase1.getBytesPerFrame());
        } else {
            LOG.logError("BytesPerFrame differ");
            return null;
        }
        if (ase1.getBytesPerPacket() == ase2.getBytesPerPacket()) {
            ase.setBytesPerPacket(ase1.getBytesPerPacket());
        } else {
            return null;
        }
        if (ase1.getBytesPerSample() == ase2.getBytesPerSample()) {
            ase.setBytesPerSample(ase1.getBytesPerSample());
        } else {
            LOG.logError("BytesPerSample differ");
            return null;
        }
        if (ase1.getChannelCount() == ase2.getChannelCount()) {
            ase.setChannelCount(ase1.getChannelCount());
        } else {
            return null;
        }
        if (ase1.getPacketSize() == ase2.getPacketSize()) {
            ase.setPacketSize(ase1.getPacketSize());
        } else {
            LOG.logError("ChannelCount differ");
            return null;
        }
        if (ase1.getCompressionId() == ase2.getCompressionId()) {
            ase.setCompressionId(ase1.getCompressionId());
        } else {
            return null;
        }
        if (ase1.getSampleRate() == ase2.getSampleRate()) {
            ase.setSampleRate(ase1.getSampleRate());
        } else {
            return null;
        }
        if (ase1.getSampleSize() == ase2.getSampleSize()) {
            ase.setSampleSize(ase1.getSampleSize());
        } else {
            return null;
        }
        if (ase1.getSamplesPerPacket() == ase2.getSamplesPerPacket()) {
            ase.setSamplesPerPacket(ase1.getSamplesPerPacket());
        } else {
            return null;
        }
        if (ase1.getSoundVersion() == ase2.getSoundVersion()) {
            ase.setSoundVersion(ase1.getSoundVersion());
        } else {
            return null;
        }
        if (Arrays.equals(ase1.getSoundVersion2Data(), ase2.getSoundVersion2Data())) {
            ase.setSoundVersion2Data(ase1.getSoundVersion2Data());
        } else {
            return null;
        }
        if (ase1.getBoxes().size() == ase2.getBoxes().size()) {
            Iterator<Box> bxs1 = ase1.getBoxes().iterator();
            Iterator<Box> bxs2 = ase2.getBoxes().iterator();
            while (bxs1.hasNext()) {
                Box cur1 = bxs1.next();
                Box cur2 = bxs2.next();
                ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
                ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
                try {
                    cur1.getBox(Channels.newChannel(baos1));
                    cur2.getBox(Channels.newChannel(baos2));
                } catch (IOException e) {
                    LOG.logWarn(e.getMessage());
                    return null;
                }
                if (Arrays.equals(baos1.toByteArray(), baos2.toByteArray())) {
                    ase.addBox(cur1);
                } else {
                    if (ESDescriptorBox.TYPE.equals(cur1.getType()) && ESDescriptorBox.TYPE.equals(cur2.getType())) {
                        ESDescriptorBox esdsBox1 = (ESDescriptorBox) cur1;
                        ESDescriptorBox esdsBox2 = (ESDescriptorBox) cur2;
                        ESDescriptor esd = mergeDescriptors(esdsBox1.getEsDescriptor(), esdsBox2.getEsDescriptor());
                        esdsBox1.setDescriptor(esd);
                        ase.addBox(cur1);
                    }
                }
            }
        }
        return ase;

    }


    private ESDescriptor mergeDescriptors(BaseDescriptor des1, BaseDescriptor des2) {
        if (des1 instanceof ESDescriptor && des2 instanceof ESDescriptor) {
            ESDescriptor esds1 = (ESDescriptor) des1;
            ESDescriptor esds2 = (ESDescriptor) des2;
            if (esds1.getURLFlag() != esds2.getURLFlag()) {
                return null;
            }
            if (esds1.getURLLength() != esds2.getURLLength()) {
                // ignore different urls
            }
            if (esds1.getDependsOnEsId() != esds2.getDependsOnEsId()) {
                return null;
            }
            if (esds1.getEsId() != esds2.getEsId()) {
                return null;
            }
            if (esds1.getoCREsId() != esds2.getoCREsId()) {
                return null;
            }
            if (esds1.getoCRstreamFlag() != esds2.getoCRstreamFlag()) {
                return null;
            }
            if (esds1.getRemoteODFlag() != esds2.getRemoteODFlag()) {
                return null;
            }
            if (esds1.getStreamDependenceFlag() != esds2.getStreamDependenceFlag()) {
                return null;
            }
            if (esds1.getStreamPriority() != esds2.getStreamPriority()) {
                // use stream prio of first (why not)
            }
            if (esds1.getURLString() != null ? !esds1.getURLString().equals(esds2.getURLString()) : esds2.getURLString() != null) {
                // ignore different urls
            }
            if (esds1.getDecoderConfigDescriptor() != null ? !esds1.getDecoderConfigDescriptor().equals(esds2.getDecoderConfigDescriptor()) : esds2.getDecoderConfigDescriptor() != null) {
                DecoderConfigDescriptor dcd1 = esds1.getDecoderConfigDescriptor();
                DecoderConfigDescriptor dcd2 = esds2.getDecoderConfigDescriptor();

                if (dcd1.getAudioSpecificInfo() != null && dcd2.getAudioSpecificInfo() != null && !dcd1.getAudioSpecificInfo().equals(dcd2.getAudioSpecificInfo())) {
                    return null;
                }
                if (dcd1.getAvgBitRate() != dcd2.getAvgBitRate()) {
                    dcd1.setAvgBitRate((dcd1.getAvgBitRate() + dcd2.getAvgBitRate()) / 2);
                }
                if (dcd1.getBufferSizeDB() != dcd2.getBufferSizeDB()) {
                    // I don't care
                }

                if (dcd1.getDecoderSpecificInfo() != null ? !dcd1.getDecoderSpecificInfo().equals(dcd2.getDecoderSpecificInfo()) : dcd2.getDecoderSpecificInfo() != null) {
                    return null;
                }

                if (dcd1.getMaxBitRate() != dcd2.getMaxBitRate()) {
                    dcd1.setMaxBitRate(Math.max(dcd1.getMaxBitRate(), dcd2.getMaxBitRate()));
                }
                if (!dcd1.getProfileLevelIndicationDescriptors().equals(dcd2.getProfileLevelIndicationDescriptors())) {
                    return null;
                }

                if (dcd1.getObjectTypeIndication() != dcd2.getObjectTypeIndication()) {
                    return null;
                }
                if (dcd1.getStreamType() != dcd2.getStreamType()) {
                    return null;
                }
                if (dcd1.getUpStream() != dcd2.getUpStream()) {
                    return null;
                }

            }
            if (esds1.getOtherDescriptors() != null ? !esds1.getOtherDescriptors().equals(esds2.getOtherDescriptors()) : esds2.getOtherDescriptors() != null) {
                return null;
            }
            if (esds1.getSlConfigDescriptor() != null ? !esds1.getSlConfigDescriptor().equals(esds2.getSlConfigDescriptor()) : esds2.getSlConfigDescriptor() != null) {
                return null;
            }
            return esds1;
        } else {
            LOG.logError("I can only merge ESDescriptors");
            return null;
        }
    }

    public List<Sample> getSamples() {
        ArrayList<Sample> lists = new ArrayList<Sample>();

        for (Track track : tracks) {
            lists.addAll(track.getSamples());
        }

        return lists;
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return stsd;
    }

    public synchronized long[] getSampleDurations() {
        int numSamples = 0;
        for (Track track : tracks) {
            numSamples += track.getSampleDurations().length;
        }
        long[] decodingTimes = new long[numSamples];
        int index = 0;
        // should use system arraycopy but this works too (yes it's slow ...)
        for (Track track : tracks) {
            for (long l : track.getSampleDurations()) {
                decodingTimes[index++] = l;
            }
        }
        return decodingTimes;
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        if (tracks[0].getCompositionTimeEntries() != null && !tracks[0].getCompositionTimeEntries().isEmpty()) {
            List<int[]> lists = new LinkedList<int[]>();
            for (Track track : tracks) {
                lists.add(CompositionTimeToSample.blowupCompositionTimes(track.getCompositionTimeEntries()));
            }
            LinkedList<CompositionTimeToSample.Entry> compositionTimeEntries = new LinkedList<CompositionTimeToSample.Entry>();
            for (int[] list : lists) {
                for (int compositionTime : list) {
                    if (compositionTimeEntries.isEmpty() || compositionTimeEntries.getLast().getOffset() != compositionTime) {
                        CompositionTimeToSample.Entry e = new CompositionTimeToSample.Entry(1, compositionTime);
                        compositionTimeEntries.add(e);
                    } else {
                        CompositionTimeToSample.Entry e = compositionTimeEntries.getLast();
                        e.setCount(e.getCount() + 1);
                    }
                }
            }
            return compositionTimeEntries;
        } else {
            return null;
        }
    }

    public long[] getSyncSamples() {
        if (tracks[0].getSyncSamples() != null && tracks[0].getSyncSamples().length > 0) {
            int numSyncSamples = 0;
            for (Track track : tracks) {
                numSyncSamples += track.getSyncSamples()!=null?track.getSyncSamples().length:0;
            }
            long[] returnSyncSamples = new long[numSyncSamples];

            int pos = 0;
            long samplesBefore = 0;
            for (Track track : tracks) {
                if (track.getSyncSamples()!=null) {
                    for (long l : track.getSyncSamples()) {
                        returnSyncSamples[pos++] = samplesBefore + l;
                    }
                }
                samplesBefore += track.getSamples().size();
            }
            return returnSyncSamples;
        } else {
            return null;
        }
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        if (tracks[0].getSampleDependencies() != null && !tracks[0].getSampleDependencies().isEmpty()) {
            List<SampleDependencyTypeBox.Entry> list = new LinkedList<SampleDependencyTypeBox.Entry>();
            for (Track track : tracks) {
                list.addAll(track.getSampleDependencies());
            }
            return list;
        } else {
            return null;
        }
    }

    public TrackMetaData getTrackMetaData() {
        return tracks[0].getTrackMetaData();
    }

    public String getHandler() {
        return tracks[0].getHandler();
    }

    public SubSampleInformationBox getSubsampleInformationBox() {
        return tracks[0].getSubsampleInformationBox();
    }

}
