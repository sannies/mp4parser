package org.mp4parser.muxer.tracks;

import org.mp4parser.boxes.dolby.EC3SpecificBox;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.BitReaderBuffer;
import org.mp4parser.boxes.iso14496.part12.CompositionTimeToSample;
import org.mp4parser.boxes.iso14496.part12.SampleDependencyTypeBox;
import org.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import org.mp4parser.boxes.iso14496.part12.SubSampleInformationBox;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.muxer.AbstractTrack;
import org.mp4parser.muxer.DataSource;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.TrackMetaData;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static org.mp4parser.tools.CastUtils.l2i;

/**
 * Created by IntelliJ IDEA.
 * User: magnus
 * Date: 2012-03-14
 * Time: 10:39
 * To change this template use File | Settings | File Templates.
 */
public class EC3TrackImpl extends AbstractTrack {
    private static final long MAX_FRAMES_PER_MMAP = 20;
    private final DataSource dataSource;
    TrackMetaData trackMetaData = new TrackMetaData();
    AudioSampleEntry audioSampleEntry;

    private int bitrate;
    private int frameSize;

    private List<BitStreamInfo> bitStreamInfos = new LinkedList<BitStreamInfo>();
    private List<Sample> samples;
    private long[] decodingTimes;

    public EC3TrackImpl(DataSource dataSource) throws IOException {
        super(dataSource.toString());
        this.dataSource = dataSource;

        boolean done = false;

        while (!done) {
            BitStreamInfo bsi = readVariables();
            if (bsi == null) {
                throw new IOException();
            }
            for (BitStreamInfo entry : bitStreamInfos) {
                if (bsi.strmtyp != 1 && entry.substreamid == bsi.substreamid) {
                    done = true;
                }
            }
            if (!done) {
                bitStreamInfos.add(bsi);
            }
        }


        if (bitStreamInfos.size() == 0) {
            throw new IOException();
        }
        int samplerate = bitStreamInfos.get(0).samplerate;

        audioSampleEntry = new AudioSampleEntry("ec-3");
        audioSampleEntry.setChannelCount(2);  // According to  ETSI TS 102 366 Annex F
        audioSampleEntry.setSampleRate(samplerate);
        audioSampleEntry.setDataReferenceIndex(1);
        audioSampleEntry.setSampleSize(16);

        EC3SpecificBox ec3 = new EC3SpecificBox();
        int[] deps = new int[bitStreamInfos.size()];
        int[] chan_locs = new int[bitStreamInfos.size()];
        for (BitStreamInfo bsi : bitStreamInfos) {
            if (bsi.strmtyp == 1) {
                deps[bsi.substreamid]++;
                chan_locs[bsi.substreamid] = ((bsi.chanmap >> 6) & 0x100) | ((bsi.chanmap >> 5) & 0xff);
            }
        }
        for (BitStreamInfo bsi : bitStreamInfos) {
            if (bsi.strmtyp != 1) {
                EC3SpecificBox.Entry e = new EC3SpecificBox.Entry();
                e.fscod = bsi.fscod;
                e.bsid = bsi.bsid;
                e.bsmod = bsi.bsmod;
                e.acmod = bsi.acmod;
                e.lfeon = bsi.lfeon;
                e.reserved = 0;
                e.num_dep_sub = deps[bsi.substreamid];
                e.chan_loc = chan_locs[bsi.substreamid];
                e.reserved2 = 0;
                ec3.addEntry(e);
            }
            bitrate += bsi.bitrate;
            frameSize += bsi.frameSize;
        }

        ec3.setDataRate(bitrate / 1000);
        audioSampleEntry.addBox(ec3);

        trackMetaData.setCreationTime(new Date());
        trackMetaData.setModificationTime(new Date());

        trackMetaData.setTimescale(samplerate); // Audio tracks always use samplerate as timescale
        trackMetaData.setVolume(1);

        ((Buffer)dataSource).position(0);
        samples = readSamples();
        this.decodingTimes = new long[samples.size()];
        Arrays.fill(decodingTimes, 1536);
    }

    public void close() throws IOException {
        dataSource.close();
    }

    public List<Sample> getSamples() {

        return samples;
    }

    public List<SampleEntry> getSampleEntries() {
        return Collections.<SampleEntry>singletonList(audioSampleEntry);
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return null;
    }

    public long[] getSyncSamples() {
        return null;
    }

    public long[] getSampleDurations() {
        return decodingTimes;
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

    public SubSampleInformationBox getSubsampleInformationBox() {
        return null;
    }

    private BitStreamInfo readVariables() throws IOException {
        long startPosition = dataSource.position();
        ByteBuffer bb = ByteBuffer.allocate(200);
        dataSource.read(bb);
        ((Buffer)bb).rewind();

        BitReaderBuffer brb = new BitReaderBuffer(bb);
        int syncword = brb.readBits(16);
        if (syncword != 0xb77) {
            return null;
        }

        BitStreamInfo entry = new BitStreamInfo();

        entry.strmtyp = brb.readBits(2);
        entry.substreamid = brb.readBits(3);
        int frmsiz = brb.readBits(11);
        entry.frameSize = 2 * (frmsiz + 1);

        entry.fscod = brb.readBits(2);
        int fscod2 = -1;
        int numblkscod;
        if (entry.fscod == 3) {
            fscod2 = brb.readBits(2);
            numblkscod = 3;
        } else {
            numblkscod = brb.readBits(2);
        }
        int numberOfBlocksPerSyncFrame = 0;
        switch (numblkscod) {
            case 0:
                numberOfBlocksPerSyncFrame = 1;
                break;

            case 1:
                numberOfBlocksPerSyncFrame = 2;
                break;

            case 2:
                numberOfBlocksPerSyncFrame = 3;
                break;

            case 3:
                numberOfBlocksPerSyncFrame = 6;
                break;

        }
        entry.frameSize *= (6 / numberOfBlocksPerSyncFrame);

        entry.acmod = brb.readBits(3);
        entry.lfeon = brb.readBits(1);
        entry.bsid = brb.readBits(5);
        brb.readBits(5);
        if (1 == brb.readBits(1)) {
            brb.readBits(8); // compr
        }
        if (0 == entry.acmod) {
            brb.readBits(5);
            if (1 == brb.readBits(1)) {
                brb.readBits(8);
            }
        }
        if (1 == entry.strmtyp) {
            if (1 == brb.readBits(1)) {
                entry.chanmap = brb.readBits(16);
            }
        }
        if (1 == brb.readBits(1)) {     // mixing metadata
            if (entry.acmod > 2) {
                brb.readBits(2);
            }
            if (1 == (entry.acmod & 1) && entry.acmod > 2) {
                brb.readBits(3);
                brb.readBits(3);
            }
            if (0 < (entry.acmod & 4)) {
                brb.readBits(3);
                brb.readBits(3);
            }
            if (1 == entry.lfeon) {
                if (1 == brb.readBits(1)) {
                    brb.readBits(5);
                }
            }
            if (0 == entry.strmtyp) {
                if (1 == brb.readBits(1)) {
                    brb.readBits(6);
                }
                if (0 == entry.acmod) {
                    if (1 == brb.readBits(1)) {
                        brb.readBits(6);
                    }
                }
                if (1 == brb.readBits(1)) {
                    brb.readBits(6);
                }
                int mixdef = brb.readBits(2);
                if (1 == mixdef) {
                    brb.readBits(5);
                } else if (2 == mixdef) {
                    brb.readBits(12);
                } else if (3 == mixdef) {
                    int mixdeflen = brb.readBits(5);
                    if (1 == brb.readBits(1)) {
                        brb.readBits(5);
                        if (1 == brb.readBits(1)) {
                            brb.readBits(4);
                        }
                        if (1 == brb.readBits(1)) {
                            brb.readBits(4);
                        }
                        if (1 == brb.readBits(1)) {
                            brb.readBits(4);
                        }
                        if (1 == brb.readBits(1)) {
                            brb.readBits(4);
                        }
                        if (1 == brb.readBits(1)) {
                            brb.readBits(4);
                        }
                        if (1 == brb.readBits(1)) {
                            brb.readBits(4);
                        }
                        if (1 == brb.readBits(1)) {
                            brb.readBits(4);
                        }
                        if (1 == brb.readBits(1)) {
                            if (1 == brb.readBits(1)) {
                                brb.readBits(4);
                            }
                            if (1 == brb.readBits(1)) {
                                brb.readBits(4);
                            }
                        }
                    }
                    if (1 == brb.readBits(1)) {
                        brb.readBits(5);
                        if (1 == brb.readBits(1)) {
                            brb.readBits(7);
                            if (1 == brb.readBits(1)) {
                                brb.readBits(8);
                            }
                        }
                    }
                    for (int i = 0; i < (mixdeflen + 2); i++) {
                        brb.readBits(8);
                    }
                    brb.byteSync();
                }
                if (entry.acmod < 2) {
                    if (1 == brb.readBits(1)) {
                        brb.readBits(14);
                    }
                    if (0 == entry.acmod) {
                        if (1 == brb.readBits(1)) {
                            brb.readBits(14);
                        }
                    }
                    if (1 == brb.readBits(1)) {
                        if (numblkscod == 0) {
                            brb.readBits(5);
                        } else {
                            for (int i = 0; i < numberOfBlocksPerSyncFrame; i++) {
                                if (1 == brb.readBits(1)) {
                                    brb.readBits(5);
                                }
                            }
                        }

                    }
                }
            }
        }
        if (1 == brb.readBits(1)) { // infomdate
            entry.bsmod = brb.readBits(3);
        }

        switch (entry.fscod) {
            case 0:
                entry.samplerate = 48000;
                break;

            case 1:
                entry.samplerate = 44100;
                break;

            case 2:
                entry.samplerate = 32000;
                break;

            case 3: {
                switch (fscod2) {
                    case 0:
                        entry.samplerate = 24000;
                        break;

                    case 1:
                        entry.samplerate = 22050;
                        break;

                    case 2:
                        entry.samplerate = 16000;
                        break;

                    case 3:
                        entry.samplerate = 0;
                        break;
                }
                break;
            }

        }
        if (entry.samplerate == 0) {
            return null;
        }

        entry.bitrate = (int) (((double) entry.samplerate) / 1536.0 * entry.frameSize * 8);

        dataSource.position(startPosition + entry.frameSize);
        return entry;
    }

    private List<Sample> readSamples() throws IOException {
        final int framesLeft = l2i((dataSource.size() - dataSource.position()) / frameSize);
        List<Sample> mySamples = new ArrayList<Sample>(framesLeft);
        for (int i = 0; i < framesLeft; i++) {
            final int start = i * frameSize;
            mySamples.add(new Sample() {
                public void writeTo(WritableByteChannel channel) throws IOException {
                    dataSource.transferTo(start, frameSize, channel);
                }

                public long getSize() {
                    return frameSize;
                }

                public ByteBuffer asByteBuffer() {
                    try {
                        return dataSource.map(start, frameSize);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public SampleEntry getSampleEntry() {
                    return audioSampleEntry;
                }
            });
        }

        return mySamples;
    }

    @Override
    public String toString() {
        return "EC3TrackImpl{" +
                "bitrate=" + bitrate +
                ", bitStreamInfos=" + bitStreamInfos +
                '}';
    }

    public static class BitStreamInfo extends EC3SpecificBox.Entry {
        public int frameSize;
        public int substreamid;
        public int bitrate;
        public int samplerate;
        public int strmtyp;
        public int chanmap;

        @Override
        public String toString() {
            return "BitStreamInfo{" +
                    "frameSize=" + frameSize +
                    ", substreamid=" + substreamid +
                    ", bitrate=" + bitrate +
                    ", samplerate=" + samplerate +
                    ", strmtyp=" + strmtyp +
                    ", chanmap=" + chanmap +
                    '}';
        }
    }
}
