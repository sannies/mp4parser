/*
 * Copyright 2012 castLabs GmbH, Berlin
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

import com.mp4parser.muxer.DataSource;
import com.mp4parser.boxes.iso14496.part12.SubSampleInformationBox;
import com.mp4parser.boxes.sampleentry.AudioSampleEntry;
import com.mp4parser.muxer.AbstractTrack;
import com.mp4parser.muxer.Sample;
import com.mp4parser.muxer.TrackMetaData;
import com.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import com.mp4parser.boxes.iso14496.part1.objectdescriptors.*;
import com.mp4parser.boxes.iso14496.part12.CompositionTimeToSample;
import com.mp4parser.boxes.iso14496.part12.SampleDependencyTypeBox;
import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.*;

/**
 */
public class AACTrackImpl extends AbstractTrack {

    static Map<Integer, String> audioObjectTypes = new HashMap<Integer, String>();

    static {
        audioObjectTypes.put(1, "AAC Main");
        audioObjectTypes.put(2, "AAC LC (Low Complexity)");
        audioObjectTypes.put(3, "AAC SSR (Scalable Sample Rate)");
        audioObjectTypes.put(4, "AAC LTP (Long Term Prediction)");
        audioObjectTypes.put(5, "SBR (Spectral Band Replication)");
        audioObjectTypes.put(6, "AAC Scalable");
        audioObjectTypes.put(7, "TwinVQ");
        audioObjectTypes.put(8, "CELP (Code Excited Linear Prediction)");
        audioObjectTypes.put(9, "HXVC (Harmonic Vector eXcitation Coding)");
        audioObjectTypes.put(10, "Reserved");
        audioObjectTypes.put(11, "Reserved");
        audioObjectTypes.put(12, "TTSI (Text-To-Speech Interface)");
        audioObjectTypes.put(13, "Main Synthesis");
        audioObjectTypes.put(14, "Wavetable Synthesis");
        audioObjectTypes.put(15, "General MIDI");
        audioObjectTypes.put(16, "Algorithmic Synthesis and Audio Effects");
        audioObjectTypes.put(17, "ER (Error Resilient) AAC LC");
        audioObjectTypes.put(18, "Reserved");
        audioObjectTypes.put(19, "ER AAC LTP");
        audioObjectTypes.put(20, "ER AAC Scalable");
        audioObjectTypes.put(21, "ER TwinVQ");
        audioObjectTypes.put(22, "ER BSAC (Bit-Sliced Arithmetic Coding)");
        audioObjectTypes.put(23, "ER AAC LD (Low Delay)");
        audioObjectTypes.put(24, "ER CELP");
        audioObjectTypes.put(25, "ER HVXC");
        audioObjectTypes.put(26, "ER HILN (Harmonic and Individual Lines plus Noise)");
        audioObjectTypes.put(27, "ER Parametric");
        audioObjectTypes.put(28, "SSC (SinuSoidal Coding)");
        audioObjectTypes.put(29, "PS (Parametric Stereo)");
        audioObjectTypes.put(30, "MPEG Surround");
        audioObjectTypes.put(31, "(Escape value)");
        audioObjectTypes.put(32, "Layer-1");
        audioObjectTypes.put(33, "Layer-2");
        audioObjectTypes.put(34, "Layer-3");
        audioObjectTypes.put(35, "DST (Direct Stream Transfer)");
        audioObjectTypes.put(36, "ALS (Audio Lossless)");
        audioObjectTypes.put(37, "SLS (Scalable LosslesS)");
        audioObjectTypes.put(38, "SLS non-core");
        audioObjectTypes.put(39, "ER AAC ELD (Enhanced Low Delay)");
        audioObjectTypes.put(40, "SMR (Symbolic Music Representation) Simple");
        audioObjectTypes.put(41, "SMR Main");
        audioObjectTypes.put(42, "USAC (Unified Speech and Audio Coding) (no SBR)");
        audioObjectTypes.put(43, "SAOC (Spatial Audio Object Coding)");
        audioObjectTypes.put(44, "LD MPEG Surround");
        audioObjectTypes.put(45, "USAC");
    }

    public static Map<Integer, Integer> samplingFrequencyIndexMap = new HashMap<Integer, Integer>();

    static {
        samplingFrequencyIndexMap.put(96000, 0);
        samplingFrequencyIndexMap.put(88200, 1);
        samplingFrequencyIndexMap.put(64000, 2);
        samplingFrequencyIndexMap.put(48000, 3);
        samplingFrequencyIndexMap.put(44100, 4);
        samplingFrequencyIndexMap.put(32000, 5);
        samplingFrequencyIndexMap.put(24000, 6);
        samplingFrequencyIndexMap.put(22050, 7);
        samplingFrequencyIndexMap.put(16000, 8);
        samplingFrequencyIndexMap.put(12000, 9);
        samplingFrequencyIndexMap.put(11025, 10);
        samplingFrequencyIndexMap.put(8000, 11);
        samplingFrequencyIndexMap.put(0x0, 96000);
        samplingFrequencyIndexMap.put(0x1, 88200);
        samplingFrequencyIndexMap.put(0x2, 64000);
        samplingFrequencyIndexMap.put(0x3, 48000);
        samplingFrequencyIndexMap.put(0x4, 44100);
        samplingFrequencyIndexMap.put(0x5, 32000);
        samplingFrequencyIndexMap.put(0x6, 24000);
        samplingFrequencyIndexMap.put(0x7, 22050);
        samplingFrequencyIndexMap.put(0x8, 16000);
        samplingFrequencyIndexMap.put(0x9, 12000);
        samplingFrequencyIndexMap.put(0xa, 11025);
        samplingFrequencyIndexMap.put(0xb, 8000);
    }

    TrackMetaData trackMetaData = new TrackMetaData();
    SampleDescriptionBox sampleDescriptionBox;
    long[] decTimes;
    AdtsHeader firstHeader;

    int bufferSizeDB;
    long maxBitRate;
    long avgBitRate;

    private DataSource dataSource;
    private List<Sample> samples;
    private String lang = "eng";

    public void close() throws IOException {
        // doing everything to get rid of references to memory mapped things
        dataSource.close();
    }

    public AACTrackImpl(DataSource dataSource) throws IOException {
        this(dataSource, "eng");
    }



    public AACTrackImpl(DataSource dataSource, String lang) throws IOException {
        super(dataSource.toString());
        this.lang = lang;
        this.dataSource = dataSource;
        samples = new ArrayList<Sample>();
        firstHeader = readSamples(dataSource);

        double packetsPerSecond = (double) firstHeader.sampleRate / 1024.0;
        double duration = samples.size() / packetsPerSecond;

        long dataSize = 0;
        LinkedList<Integer> queue = new LinkedList<Integer>();
        for (Sample sample : samples) {
            int size = (int) sample.getSize();
            dataSize += size;
            queue.add(size);
            while (queue.size() > packetsPerSecond) {
                queue.pop();
            }
            if (queue.size() == (int) packetsPerSecond) {
                int currSize = 0;
                for (Integer aQueue : queue) {
                    currSize += aQueue;
                }
                double currBitrate = 8.0 * currSize / queue.size() * packetsPerSecond;
                if (currBitrate > maxBitRate) {
                    maxBitRate = (int) currBitrate;
                }
            }
        }

        avgBitRate = (int) (8 * dataSize / duration);

        bufferSizeDB = 1536; /* TODO: Calcultate this somehow! */

        sampleDescriptionBox = new SampleDescriptionBox();
        AudioSampleEntry audioSampleEntry = new AudioSampleEntry("mp4a");
        if (firstHeader.channelconfig == 7) {
            audioSampleEntry.setChannelCount(8);
        } else {
            audioSampleEntry.setChannelCount(firstHeader.channelconfig);
        }
        audioSampleEntry.setSampleRate(firstHeader.sampleRate);
        audioSampleEntry.setDataReferenceIndex(1);
        audioSampleEntry.setSampleSize(16);


        ESDescriptorBox esds = new ESDescriptorBox();
        ESDescriptor descriptor = new ESDescriptor();
        descriptor.setEsId(0);

        SLConfigDescriptor slConfigDescriptor = new SLConfigDescriptor();
        slConfigDescriptor.setPredefined(2);
        descriptor.setSlConfigDescriptor(slConfigDescriptor);

        DecoderConfigDescriptor decoderConfigDescriptor = new DecoderConfigDescriptor();
        decoderConfigDescriptor.setObjectTypeIndication(0x40);
        decoderConfigDescriptor.setStreamType(5);
        decoderConfigDescriptor.setBufferSizeDB(bufferSizeDB);
        decoderConfigDescriptor.setMaxBitRate(maxBitRate);
        decoderConfigDescriptor.setAvgBitRate(avgBitRate);

        AudioSpecificConfig audioSpecificConfig = new AudioSpecificConfig();
        audioSpecificConfig.setOriginalAudioObjectType(2); // AAC LC
        audioSpecificConfig.setSamplingFrequencyIndex(firstHeader.sampleFrequencyIndex);
        audioSpecificConfig.setChannelConfiguration(firstHeader.channelconfig);
        decoderConfigDescriptor.setAudioSpecificInfo(audioSpecificConfig);

        descriptor.setDecoderConfigDescriptor(decoderConfigDescriptor);

        esds.setEsDescriptor(descriptor);
        audioSampleEntry.addBox(esds);
        sampleDescriptionBox.addBox(audioSampleEntry);

        trackMetaData.setCreationTime(new Date());
        trackMetaData.setModificationTime(new Date());
        trackMetaData.setLanguage(lang);
        trackMetaData.setVolume(1);
        trackMetaData.setTimescale(firstHeader.sampleRate); // Audio tracks always use sampleRate as timescale
        decTimes = new long[samples.size()];
        Arrays.fill(decTimes, 1024);
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return sampleDescriptionBox;
    }

    public long[] getSampleDurations() {
        return decTimes;
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
        return "soun";
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public SubSampleInformationBox getSubsampleInformationBox() {
        return null;
    }

    class AdtsHeader {
        int getSize() {
            return 7 + (protectionAbsent == 0 ? 2 : 0);
        }

        int sampleFrequencyIndex;

        int mpegVersion;
        int layer;
        int protectionAbsent;
        int profile;
        int sampleRate;

        int channelconfig;
        int original;
        int home;
        int copyrightedStream;
        int copyrightStart;
        int frameLength;
        int bufferFullness;
        int numAacFramesPerAdtsFrame;
    }

    private AdtsHeader readADTSHeader(DataSource channel) throws IOException {
        AdtsHeader hdr = new AdtsHeader();
        ByteBuffer bb = ByteBuffer.allocate(7);
        while (bb.position() < 7) {
            if (channel.read(bb) == -1) {
                return null;
            }
        }

        BitReaderBuffer brb = new BitReaderBuffer((ByteBuffer) bb.rewind());
        int syncword = brb.readBits(12); // A
        if (syncword != 0xfff) {
            throw new IOException("Expected Start Word 0xfff");
        }
        hdr.mpegVersion = brb.readBits(1); // B
        hdr.layer = brb.readBits(2); // C
        hdr.protectionAbsent = brb.readBits(1); // D
        hdr.profile = brb.readBits(2) + 1;  // E
        //System.err.println(String.format("Profile %s", audioObjectTypes.get(hdr.profile)));
        hdr.sampleFrequencyIndex = brb.readBits(4);
        hdr.sampleRate = samplingFrequencyIndexMap.get(hdr.sampleFrequencyIndex); // F
        brb.readBits(1); // G
        hdr.channelconfig = brb.readBits(3); // H
        hdr.original = brb.readBits(1); // I
        hdr.home = brb.readBits(1); // J
        hdr.copyrightedStream = brb.readBits(1); // K
        hdr.copyrightStart = brb.readBits(1); // L
        hdr.frameLength = brb.readBits(13); // M
        //System.err.println(hdr.frameLength);
        hdr.bufferFullness = brb.readBits(11); // 54
        hdr.numAacFramesPerAdtsFrame = brb.readBits(2) + 1; // 56
        if (hdr.numAacFramesPerAdtsFrame != 1) {
            throw new IOException("This muxer can only work with 1 AAC frame per ADTS frame");
        }
        if (hdr.protectionAbsent == 0) {
            channel.read(ByteBuffer.allocate(2));
        }
        return hdr;
    }

    private AdtsHeader readSamples(DataSource channel) throws IOException {
        AdtsHeader first = null;
        AdtsHeader hdr;

        while ((hdr = readADTSHeader(channel)) != null) {
            if (first == null) {
                first = hdr;
            }

            final long currentPosition = channel.position();
            final long frameSize = hdr.frameLength - hdr.getSize();
            samples.add(new Sample() {
                public void writeTo(WritableByteChannel channel) throws IOException {
                    dataSource.transferTo(currentPosition, frameSize, channel);
                }

                public long getSize() {
                    return frameSize;
                }

                public ByteBuffer asByteBuffer() {
                    try {
                        return dataSource.map(currentPosition, frameSize);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            channel.position(channel.position() + hdr.frameLength - hdr.getSize());
        }
        return first;
    }

    @Override
    public String toString() {
        return "AACTrackImpl{" +
                "sampleRate=" + firstHeader.sampleRate +
                ", channelconfig=" + firstHeader.channelconfig +
                '}';
    }
}

