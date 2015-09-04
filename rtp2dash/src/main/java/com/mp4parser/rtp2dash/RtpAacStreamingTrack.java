package com.mp4parser.rtp2dash;

import com.mp4parser.boxes.iso14496.part1.objectdescriptors.*;
import com.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import com.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import com.mp4parser.boxes.sampleentry.AudioSampleEntry;
import com.mp4parser.muxer.DataSource;
import com.mp4parser.muxer.tracks.h264.parsing.read.BitstreamReader;
import com.mp4parser.streaming.AbstractStreamingTrack;
import com.mp4parser.streaming.StreamingSampleImpl;
import com.mp4parser.streaming.StreamingTrack;
import com.mp4parser.streaming.extensions.DefaultSampleFlagsTrackExtension;
import com.mp4parser.tools.Ascii;
import com.mp4parser.tools.Hex;
import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.Mp4Arrays;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

/**
 * Created by sannies on 01.09.2015.
 */
public class RtpAacStreamingTrack extends AbstractStreamingTrack implements Callable<Void> {
    private static final Logger LOG = Logger.getLogger(RtpAacStreamingTrack.class.getName());
    boolean isOpen = true;
    private int initialTimeout = 10000;
    private int timeout = 5000;
    CountDownLatch countDownLatch = new CountDownLatch(1);
    private int port;
    private int payloadType;
    private int sizeLength;
    private int indexLength;
    private long clockrate;
    SampleDescriptionBox stsd;
    String language = "und";

    public RtpAacStreamingTrack(int port, int payloadType, int bandwidth, String fmtp, String rtpMap) {
        String encoding = rtpMap.split("/")[0];
        clockrate = Integer.parseInt(rtpMap.split("/")[1]);
        int audioChannels = Integer.parseInt(rtpMap.split("/")[2]);


        String[] props = fmtp.split(";");
        HashMap<String, String> propMap = new HashMap<String, String>();
        for (String prop : props) {
            String[] splitProp = prop.split("=");
            propMap.put(splitProp[0].trim(), splitProp.length > 1 ? splitProp[1] : null);
        }


        this.sizeLength = Integer.parseInt(propMap.get("sizelength"));
        this.indexLength = Integer.parseInt(propMap.get("indexlength"));
        ;
        String config = propMap.get("config");
        final byte[] audioSpecificConfigFromSDP = Hex.decodeHex(config);
        assert "aac-hbr".equalsIgnoreCase(propMap.get("mode"));
        assert sizeLength + indexLength == 16;
        this.port = port;
        this.payloadType = payloadType;

        stsd = new SampleDescriptionBox();
        AudioSampleEntry audioSampleEntry = new AudioSampleEntry("mp4a");
        if (audioChannels == 7) {
            audioSampleEntry.setChannelCount(8);
        } else {
            audioSampleEntry.setChannelCount(audioChannels);
        }
        audioSampleEntry.setSampleRate(clockrate);
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
        decoderConfigDescriptor.setMaxBitRate(bandwidth * 8 * 1024);
        decoderConfigDescriptor.setAvgBitRate(bandwidth * 8 * 1024);

        AudioSpecificConfig audioSpecificConfig = new AudioSpecificConfig() {
            @Override
            protected ByteBuffer serializeConfigBytes() {
                return ByteBuffer.wrap(audioSpecificConfigFromSDP);
            }

            @Override
            protected int getContentSize() {
                return audioSpecificConfigFromSDP.length;
            }
        };
        audioSpecificConfig.setOriginalAudioObjectType(2); // AAC LC
        decoderConfigDescriptor.setAudioSpecificInfo(audioSpecificConfig);

        descriptor.setDecoderConfigDescriptor(decoderConfigDescriptor);

        esds.setEsDescriptor(descriptor);
        audioSampleEntry.addBox(esds);
        stsd.addBox(audioSampleEntry);


        DefaultSampleFlagsTrackExtension defaultSampleFlagsTrackExtension = new DefaultSampleFlagsTrackExtension();
        defaultSampleFlagsTrackExtension.setIsLeading(2);
        defaultSampleFlagsTrackExtension.setSampleDependsOn(2);
        defaultSampleFlagsTrackExtension.setSampleIsDependedOn(2);
        defaultSampleFlagsTrackExtension.setSampleHasRedundancy(2);
        defaultSampleFlagsTrackExtension.setSampleIsNonSyncSample(false);
        this.addTrackExtension(defaultSampleFlagsTrackExtension);

    }

    public Void call() throws IOException {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            socket.setSoTimeout(initialTimeout);

            byte[] buf = new byte[16384];
            LOG.info("Start Receiving AAC RTP Packets on port " + port);
            while (isOpen) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    LOG.info("Socket Timeout closed RtpAacStreamingTrack");
                    isOpen = false;
                    continue;
                }
                socket.setSoTimeout(timeout);
                BitstreamReader bsr = new BitstreamReader(new ByteArrayInputStream(packet.getData()));
                int version = (int) bsr.readNBit(2);
                boolean padding = bsr.readBool();
                boolean extension = bsr.readBool();
                int csrcCount = (int) bsr.readNBit(4);
                boolean marker = bsr.readBool();
                int payloadType = (int) bsr.readNBit(7);
                if (payloadType != this.payloadType) {
                    continue;
                }
                int sequenceNumber = (int) bsr.readNBit(16);
                byte[] payload = packet.getData();

                long rtpTimestamp = getInt(payload, 4);
                long ssrc = getInt(payload, 8);
                long[] csrc = new long[csrcCount];
                for (int i = 0; i < csrc.length; i++) {
                    csrc[i] = getInt(payload, 12 + i * 4);
                }

                int offset = 12 + csrc.length * 4;

                int auHeaderLength = (b2i(payload[offset]) << 8 >> indexLength) + (b2i(payload[offset + 1]) >> indexLength);
                int auIndex = (payload[offset + 1] >> indexLength);
                offset += 2;
                int[] sampleSizes = new int[auHeaderLength / 2];
                for (int i = 0; i < sampleSizes.length; i++) {
                    sampleSizes[i] = (b2i(payload[offset + (i * 2)]) << 8 >> indexLength) + (b2i(payload[offset + 1 + (i * 2)]) >> indexLength);
                }
                offset += auHeaderLength;
                for (int sampleSize : sampleSizes) {
                    byte[] currentSample = new byte[sampleSize];
                    System.arraycopy(payload, offset, currentSample, 0, sampleSize);
                    samples.add(new StreamingSampleImpl(ByteBuffer.wrap(currentSample), 1024));
                    //  hex(currentSample);
                    offset += sampleSize;
                }


            }
            LOG.info("Done receiving RTP Packets");
            countDownLatch.countDown();
            return null;
        } finally {
            if (isOpen) {
                LOG.warning("Stopping RTP Receiver due to exception. " + toString());
            }
            isOpen = false;
        }
    }

    public static int b2i(byte b) {
        return b < 0 ? b + 256 : b;
    }

    int getInt(byte[] d, int offset) {
        return (b2i(d[offset]) << 24) + (b2i(d[offset + 1]) << 16) + (b2i(d[offset + 2]) << 8) + (b2i(d[offset + 3]));
    }

    public static boolean isAsciiPrintable(char ch) {
        return ch >= 32 && ch < 127;
    }

    public static void hex(byte[] payload) {

        String hex = "";
        String ascii = "";
        for (byte b : payload) {
            String a = Ascii.convert(new byte[]{b});
            if (a == null || a.length() == 0) {
                a = ".";
            }
            if (!isAsciiPrintable(a.charAt(0))) {
                a = ".";
            }
            ascii += a + " ";
            hex += Hex.encodeHex(new byte[]{b}) + " ";
            if (ascii.length() == 16) {
                hex += " ";
                ascii += " ";

            }
            if (ascii.length() >= 32) {
                System.out.println(hex + " | " + ascii);
                hex = "";
                ascii = "";
            }
        }
        System.out.println(hex + " | " + ascii);
    }

    public long getTimescale() {
        return clockrate;
    }

    public boolean hasMoreSamples() {
        return samples.size() > 0 || isOpen;
    }

    public String getHandler() {
        return "soun";
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return stsd;
    }

    public void close() throws IOException {

    }

    @Override
    public String toString() {
        return "RtpAacStreamingTrack{" +
                "port=" + port +
                '}';
    }
}
