package org.mp4parser.muxer.tracks;

import org.mp4parser.boxes.iso14496.part1.objectdescriptors.BitReaderBuffer;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.DecoderConfigDescriptor;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.ESDescriptor;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.SLConfigDescriptor;
import org.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import org.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.mp4parser.muxer.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * MPEG V1 Layer 3 Audio. Does not support IDv3 or any other tags. Only raw stream of MP3 frames.
 * See <a href="http://mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm">http://mpgedit.org/mpgedit/mpeg_format/mpeghdr.htm</a>
 * for stream format description.
 *
 * @author Roman Elizarov
 */
public class MP3TrackImpl extends AbstractTrack {
    private static final int MPEG_V1 = 0x3; // only support V1
    private static final int MPEG_L3 = 1; // only support L3
    private static final int[] SAMPLE_RATE = {44100, 48000, 32000, 0};
    private static final int[] BIT_RATE = {0, 32000, 40000, 48000, 56000, 64000, 80000, 96000, 112000, 128000, 160000, 192000, 224000, 256000, 320000, 0};
    private static final int SAMPLES_PER_FRAME = 1152; // Samples per L3 frame

    private static final int ES_OBJECT_TYPE_INDICATION = 0x6b;
    private static final int ES_STREAM_TYPE = 5;
    private final DataSource dataSource;

    TrackMetaData trackMetaData = new TrackMetaData();
    SampleDescriptionBox sampleDescriptionBox;
    MP3Header firstHeader;

    long maxBitRate;
    long avgBitRate;

    private List<Sample> samples;
    private long[] durations;


    public MP3TrackImpl(DataSource channel) throws IOException {
        this(channel, "eng");
    }

    public MP3TrackImpl(DataSource dataSource, String lang) throws IOException {
        super(dataSource.toString());
        this.dataSource = dataSource;
        samples = new LinkedList<Sample>();
        firstHeader = readSamples(dataSource);

        double packetsPerSecond = (double) firstHeader.sampleRate / SAMPLES_PER_FRAME;
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
                double currBitRate = 8.0 * currSize / queue.size() * packetsPerSecond;
                if (currBitRate > maxBitRate) {
                    maxBitRate = (int) currBitRate;
                }
            }
        }

        avgBitRate = (int) (8 * dataSize / duration);

        sampleDescriptionBox = new SampleDescriptionBox();
        AudioSampleEntry audioSampleEntry = new AudioSampleEntry("mp4a");
        audioSampleEntry.setChannelCount(firstHeader.channelCount);
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
        decoderConfigDescriptor.setObjectTypeIndication(ES_OBJECT_TYPE_INDICATION);
        decoderConfigDescriptor.setStreamType(ES_STREAM_TYPE);
        decoderConfigDescriptor.setMaxBitRate(maxBitRate);
        decoderConfigDescriptor.setAvgBitRate(avgBitRate);
        descriptor.setDecoderConfigDescriptor(decoderConfigDescriptor);

        ByteBuffer data = descriptor.serialize();
        esds.setData(data);
        audioSampleEntry.addBox(esds);
        sampleDescriptionBox.addBox(audioSampleEntry);

        trackMetaData.setCreationTime(new Date());
        trackMetaData.setModificationTime(new Date());
        trackMetaData.setLanguage(lang);
        trackMetaData.setVolume(1);
        trackMetaData.setTimescale(firstHeader.sampleRate); // Audio tracks always use sampleRate as timescale
        durations = new long[samples.size()];
        Arrays.fill(durations, SAMPLES_PER_FRAME);
    }

    public void close() throws IOException {
        dataSource.close();
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return sampleDescriptionBox;
    }

    public long[] getSampleDurations() {
        return durations;
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

    private MP3Header readSamples(DataSource channel) throws IOException {
        MP3Header first = null;
        while (true) {
            long pos = channel.position();
            MP3Header hdr;
            if ((hdr = readMP3Header(channel)) == null)
                break;
            if (first == null)
                first = hdr;
            channel.position(pos);
            ByteBuffer data = ByteBuffer.allocate(hdr.getFrameLength());
            channel.read(data);
            data.rewind();
            samples.add(new SampleImpl(data));
        }
        return first;
    }

    private MP3Header readMP3Header(DataSource channel) throws IOException {
        MP3Header hdr = new MP3Header();
        ByteBuffer bb = ByteBuffer.allocate(4);
        while (bb.position() < 4) {
            if (channel.read(bb) == -1) {
                return null;
            }
        }

        BitReaderBuffer brb = new BitReaderBuffer((ByteBuffer) bb.rewind());
        int sync = brb.readBits(11); // A
        if (sync != 0x7ff)
            throw new IOException("Expected Start Word 0x7ff");
        hdr.mpegVersion = brb.readBits(2); // B

        if (hdr.mpegVersion != MPEG_V1)
            throw new IOException("Expected MPEG Version 1 (ISO/IEC 11172-3)");

        hdr.layer = brb.readBits(2); // C

        if (hdr.layer != MPEG_L3)
            throw new IOException("Expected Layer III");

        hdr.protectionAbsent = brb.readBits(1); // D

        hdr.bitRateIndex = brb.readBits(4); // E
        hdr.bitRate = BIT_RATE[hdr.bitRateIndex];
        if (hdr.bitRate == 0)
            throw new IOException("Unexpected (free/bad) bit rate");

        hdr.sampleFrequencyIndex = brb.readBits(2);
        hdr.sampleRate = SAMPLE_RATE[hdr.sampleFrequencyIndex]; // F
        if (hdr.sampleRate == 0)
            throw new IOException("Unexpected (reserved) sample rate frequency");

        hdr.padding = brb.readBits(1); // G padding
        brb.readBits(1); // H private

        hdr.channelMode = brb.readBits(2); // H
        hdr.channelCount = hdr.channelMode == 3 ? 1 : 2;
        return hdr;
    }

    @Override
    public String toString() {
        return "MP3TrackImpl";
    }

    class MP3Header {
        int mpegVersion;
        int layer;
        int protectionAbsent;

        int bitRateIndex;
        int bitRate;

        int sampleFrequencyIndex;
        int sampleRate;

        int padding;

        int channelMode;
        int channelCount;

        int getFrameLength() {
            return 144 * bitRate / sampleRate + padding;
        }
    }
}
