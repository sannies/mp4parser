package com.mp4parser.streaming.rawformats.aac;

import com.mp4parser.boxes.iso14496.part1.objectdescriptors.AudioSpecificConfig;
import com.mp4parser.boxes.iso14496.part1.objectdescriptors.DecoderConfigDescriptor;
import com.mp4parser.boxes.iso14496.part1.objectdescriptors.ESDescriptor;
import com.mp4parser.boxes.iso14496.part1.objectdescriptors.SLConfigDescriptor;
import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import com.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import com.mp4parser.boxes.sampleentry.AudioSampleEntry;
import com.mp4parser.streaming.AbstractStreamingTrack;
import com.mp4parser.streaming.StreamingSampleImpl;
import com.mp4parser.streaming.extensions.DefaultSampleFlagsTrackExtension;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * Created by sannies on 01.09.2015.
 */
public class AdtsAacStreamingTrack extends AbstractStreamingTrack implements Callable<Void> {
    private static Logger LOG = Logger.getLogger(AdtsAacStreamingTrack.class.getName());

    CountDownLatch gotFirstSample = new CountDownLatch(1);

    SampleDescriptionBox stsd = null;

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

    private InputStream is;
    private AdtsHeader firstHeader;
    private String lang = "und";
    private long avgBitrate;
    private long maxBitrate;


    public AdtsAacStreamingTrack(InputStream is, long avgBitrate, long maxBitrate) {
        this.avgBitrate = avgBitrate;
        this.maxBitrate = maxBitrate;
        assert is != null;
        this.is = is;
        DefaultSampleFlagsTrackExtension defaultSampleFlagsTrackExtension = new DefaultSampleFlagsTrackExtension();
        defaultSampleFlagsTrackExtension.setIsLeading(2);
        defaultSampleFlagsTrackExtension.setSampleDependsOn(2);
        defaultSampleFlagsTrackExtension.setSampleIsDependedOn(2);
        defaultSampleFlagsTrackExtension.setSampleHasRedundancy(2);
        defaultSampleFlagsTrackExtension.setSampleIsNonSyncSample(false);
        this.addTrackExtension(defaultSampleFlagsTrackExtension);
    }


    public synchronized SampleDescriptionBox getSampleDescriptionBox() {
        waitForFirstSample();
        if (stsd == null) {
            stsd = new SampleDescriptionBox();
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
            decoderConfigDescriptor.setBufferSizeDB(1536);
            decoderConfigDescriptor.setMaxBitRate(maxBitrate);
            decoderConfigDescriptor.setAvgBitRate(avgBitrate);

            AudioSpecificConfig audioSpecificConfig = new AudioSpecificConfig();
            audioSpecificConfig.setOriginalAudioObjectType(2); // AAC LC
            audioSpecificConfig.setSamplingFrequencyIndex(firstHeader.sampleFrequencyIndex);
            audioSpecificConfig.setChannelConfiguration(firstHeader.channelconfig);
            decoderConfigDescriptor.setAudioSpecificInfo(audioSpecificConfig);

            descriptor.setDecoderConfigDescriptor(decoderConfigDescriptor);

            esds.setEsDescriptor(descriptor);

            audioSampleEntry.addBox(esds);
            stsd.addBox(audioSampleEntry);

        }
        return stsd;
    }

    void waitForFirstSample() {
        try {
            gotFirstSample.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public long getTimescale() {
        waitForFirstSample();
        return firstHeader.sampleRate;
    }

    public boolean hasMoreSamples() {
        return false;
    }

    public String getHandler() {
        return "soun";
    }

    public String getLanguage() {
        return lang;
    }

    public void setLanguage(String lang) {
        this.lang = lang;
    }

    public void close() throws IOException {
        is.close();
    }


    private AdtsHeader readADTSHeader(InputStream fis) throws IOException {
        AdtsHeader hdr = new AdtsHeader();
        int x = fis.read(); // A
        int syncword = x << 4;
        x = fis.read();
        if (x == -1) {
            return null;
        }
        syncword += (x >> 4);
        if (syncword != 0xfff) {
            throw new IOException("Expected Start Word 0xfff");
        }
        hdr.mpegVersion = (x & 0x8) >> 3;
        hdr.layer = (x & 0x6) >> 1;
        ; // C
        hdr.protectionAbsent = (x & 0x1);  // D

        x = fis.read();


        hdr.profile = ((x & 0xc0) >> 6) + 1;  // E
        //System.err.println(String.format("Profile %s", audioObjectTypes.get(hdr.profile)));
        hdr.sampleFrequencyIndex = (x & 0x3c) >> 2;
        assert hdr.sampleFrequencyIndex != 15;
        hdr.sampleRate = samplingFrequencyIndexMap.get(hdr.sampleFrequencyIndex); // F
        hdr.channelconfig = (x & 1) << 2; // H

        x = fis.read();
        hdr.channelconfig += (x & 0xc0) >> 6;

        hdr.original = (x & 0x20) >> 5; // I
        hdr.home = (x & 0x10) >> 4; // J
        hdr.copyrightedStream = (x & 0x8) >> 3; // K
        hdr.copyrightStart = (x & 0x4) >> 2; // L
        hdr.frameLength = (x & 0x3) << 9;  // M

        x = fis.read();
        hdr.frameLength += (x << 3);

        x = fis.read();
        hdr.frameLength += (x & 0xe0) >> 5;

        hdr.bufferFullness = (x & 0x1f) << 6;

        x = fis.read();
        hdr.bufferFullness += (x & 0xfc) >> 2;
        hdr.numAacFramesPerAdtsFrame = ((x & 0x3)) + 1;


        if (hdr.numAacFramesPerAdtsFrame != 1) {
            throw new IOException("This muxer can only work with 1 AAC frame per ADTS frame");
        }
        if (hdr.protectionAbsent == 0) {
            int crc1 = fis.read();
            int crc2 = fis.read();
        }
        return hdr;
    }

    public Void call() throws Exception {
        AdtsHeader header;
        try {
            while ((header = readADTSHeader(is)) != null) {
                if (firstHeader == null) {
                    firstHeader = header;
                    gotFirstSample.countDown();
                }
                byte[] frame = new byte[header.frameLength - header.getSize()];
                int n = 0;
                while (n < frame.length) {
                    int count = is.read(frame, n, frame.length - n);
                    if (count < 0)
                        throw new EOFException();
                    n += count;
                }
                samples.add(new StreamingSampleImpl(ByteBuffer.wrap(frame), 1024));

            }
        } catch (EOFException e) {
            LOG.info("Done reading ADTS AAC file.");
        }
        return null;
    }
}
