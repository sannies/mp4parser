/*  
 * Copyright 2008 CoreMedia AG, Hamburg
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

package org.mp4parser.boxes.sampleentry;

import org.mp4parser.Box;
import org.mp4parser.BoxParser;
import org.mp4parser.boxes.iso14496.part12.ProtectionSchemeInformationBox;
import org.mp4parser.tools.CastUtils;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;

/**
 * <h1>4cc = "{@value #TYPE1}" || "{@value #TYPE2} || "{@value #TYPE3} || "{@value #TYPE4} || "{@value #TYPE5} || "{@value #TYPE7} || "{@value #TYPE8} || "{@value #TYPE9} || "{@value #TYPE10} || "{@value #TYPE11} || "{@value #TYPE12} || "{@value #TYPE13}"</h1>
 * Contains basic information about the audio samples in this track. Format-specific information
 * is appened as boxes after the data described in ISO/IEC 14496-12 chapter 8.16.2.
 */
public final class AudioSampleEntry extends AbstractSampleEntry {
    private static Logger LOG = LoggerFactory.getLogger(AudioSampleEntry.class);

    public static final String TYPE1 = "samr";
    public static final String TYPE2 = "sawb";
    public static final String TYPE3 = "mp4a";
    public static final String TYPE4 = "drms";
    public static final String TYPE5 = "alac";
    public static final String TYPE7 = "owma";
    public static final String TYPE8 = "ac-3"; /* ETSI TS 102 366 1.2.1 Annex F */
    public static final String TYPE9 = "ec-3"; /* ETSI TS 102 366 1.2.1 Annex F */
    public static final String TYPE10 = "mlpa";
    public static final String TYPE11 = "dtsl";
    public static final String TYPE12 = "dtsh";
    public static final String TYPE13 = "dtse";

    /**
     * Identifier for an encrypted audio track.
     *
     * @see ProtectionSchemeInformationBox
     */
    public static final String TYPE_ENCRYPTED = "enca";

    private int channelCount;
    private int sampleSize;
    private long sampleRate;
    private int soundVersion;
    private int compressionId;
    private int packetSize;
    private long samplesPerPacket;
    private long bytesPerPacket;
    private long bytesPerFrame;
    private long bytesPerSample;

    private int reserved1;
    private long reserved2;
    private byte[] soundVersion2Data;


    public AudioSampleEntry(String type) {
        super(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getChannelCount() {
        return channelCount;
    }

    public void setChannelCount(int channelCount) {
        this.channelCount = channelCount;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public void setSampleSize(int sampleSize) {
        this.sampleSize = sampleSize;
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(long sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getSoundVersion() {
        return soundVersion;
    }

    public void setSoundVersion(int soundVersion) {
        this.soundVersion = soundVersion;
    }

    public int getCompressionId() {
        return compressionId;
    }

    public void setCompressionId(int compressionId) {
        this.compressionId = compressionId;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public void setPacketSize(int packetSize) {
        this.packetSize = packetSize;
    }

    public long getSamplesPerPacket() {
        return samplesPerPacket;
    }

    public void setSamplesPerPacket(long samplesPerPacket) {
        this.samplesPerPacket = samplesPerPacket;
    }

    public long getBytesPerPacket() {
        return bytesPerPacket;
    }

    public void setBytesPerPacket(long bytesPerPacket) {
        this.bytesPerPacket = bytesPerPacket;
    }

    public long getBytesPerFrame() {
        return bytesPerFrame;
    }

    public void setBytesPerFrame(long bytesPerFrame) {
        this.bytesPerFrame = bytesPerFrame;
    }

    public long getBytesPerSample() {
        return bytesPerSample;
    }

    public void setBytesPerSample(long bytesPerSample) {
        this.bytesPerSample = bytesPerSample;
    }

    public byte[] getSoundVersion2Data() {
        return soundVersion2Data;
    }

    public void setSoundVersion2Data(byte[] soundVersion2Data) {
        this.soundVersion2Data = soundVersion2Data;
    }

    public int getReserved1() {
        return reserved1;
    }

    public void setReserved1(int reserved1) {
        this.reserved1 = reserved1;
    }

    public long getReserved2() {
        return reserved2;
    }

    public void setReserved2(long reserved2) {
        this.reserved2 = reserved2;
    }

    @Override
    public void parse(ReadableByteChannel dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        ByteBuffer content = ByteBuffer.allocate(28);
        dataSource.read(content);
        ((Buffer)content).position(6);
        dataReferenceIndex = IsoTypeReader.readUInt16(content);

        // 8 bytes already parsed
        //reserved bits - used by qt
        soundVersion = IsoTypeReader.readUInt16(content);

        //reserved
        reserved1 = IsoTypeReader.readUInt16(content);
        reserved2 = IsoTypeReader.readUInt32(content);

        channelCount = IsoTypeReader.readUInt16(content);
        sampleSize = IsoTypeReader.readUInt16(content);
        //reserved bits - used by qt
        compressionId = IsoTypeReader.readUInt16(content);
        //reserved bits - used by qt
        packetSize = IsoTypeReader.readUInt16(content);
        //sampleRate = in.readFixedPoint1616();
        sampleRate = IsoTypeReader.readUInt32(content);
        if (!type.equals("mlpa")) {
            sampleRate = sampleRate >>> 16;
        }

        //more qt stuff - see http://mp4v2.googlecode.com/svn-history/r388/trunk/src/atom_sound.cpp

        if (soundVersion == 1) {
            ByteBuffer appleStuff = ByteBuffer.allocate(16);
            dataSource.read(appleStuff);
            ((Buffer)appleStuff).rewind();
            samplesPerPacket = IsoTypeReader.readUInt32(appleStuff);
            bytesPerPacket = IsoTypeReader.readUInt32(appleStuff);
            bytesPerFrame = IsoTypeReader.readUInt32(appleStuff);
            bytesPerSample = IsoTypeReader.readUInt32(appleStuff);
        }
        if (soundVersion == 2) {
            ByteBuffer appleStuff = ByteBuffer.allocate(36);
            dataSource.read(appleStuff);
            ((Buffer)appleStuff).rewind();
            samplesPerPacket = IsoTypeReader.readUInt32(appleStuff);
            bytesPerPacket = IsoTypeReader.readUInt32(appleStuff);
            bytesPerFrame = IsoTypeReader.readUInt32(appleStuff);
            bytesPerSample = IsoTypeReader.readUInt32(appleStuff);
            soundVersion2Data = new byte[20];
            appleStuff.get(soundVersion2Data);
        }

        if ("owma".equals(type)) {
            LOG.error("owma");
            final long remaining = contentSize - 28
                    - (soundVersion == 1 ? 16 : 0)
                    - (soundVersion == 2 ? 36 : 0);
            final ByteBuffer owmaSpecifics = ByteBuffer.allocate(CastUtils.l2i(remaining));
            dataSource.read(owmaSpecifics);

            addBox(new Box() {

                public long getSize() {
                    return remaining;
                }

                public String getType() {
                    return "----";
                }

                public void getBox(WritableByteChannel writableByteChannel) throws IOException {
                    ((Buffer)owmaSpecifics).rewind();
                    writableByteChannel.write(owmaSpecifics);
                }

            });
        } else {
            initContainer(dataSource,
                    contentSize - 28
                            - (soundVersion == 1 ? 16 : 0)
                            - (soundVersion == 2 ? 36 : 0), boxParser);
        }
    }

    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());
        ByteBuffer byteBuffer = ByteBuffer.allocate(28
                + (soundVersion == 1 ? 16 : 0)
                + (soundVersion == 2 ? 36 : 0));
        ((Buffer)byteBuffer).position(6);
        IsoTypeWriter.writeUInt16(byteBuffer, dataReferenceIndex);
        IsoTypeWriter.writeUInt16(byteBuffer, soundVersion);
        IsoTypeWriter.writeUInt16(byteBuffer, reserved1);
        IsoTypeWriter.writeUInt32(byteBuffer, reserved2);
        IsoTypeWriter.writeUInt16(byteBuffer, channelCount);
        IsoTypeWriter.writeUInt16(byteBuffer, sampleSize);
        IsoTypeWriter.writeUInt16(byteBuffer, compressionId);
        IsoTypeWriter.writeUInt16(byteBuffer, packetSize);
        //isos.writeFixedPoint1616(getSampleRate());
        if (type.equals("mlpa")) {
            IsoTypeWriter.writeUInt32(byteBuffer, getSampleRate());
        } else {
            IsoTypeWriter.writeUInt32(byteBuffer, getSampleRate() << 16);
        }

        if (soundVersion == 1) {
            IsoTypeWriter.writeUInt32(byteBuffer, samplesPerPacket);
            IsoTypeWriter.writeUInt32(byteBuffer, bytesPerPacket);
            IsoTypeWriter.writeUInt32(byteBuffer, bytesPerFrame);
            IsoTypeWriter.writeUInt32(byteBuffer, bytesPerSample);
        }

        if (soundVersion == 2) {
            IsoTypeWriter.writeUInt32(byteBuffer, samplesPerPacket);
            IsoTypeWriter.writeUInt32(byteBuffer, bytesPerPacket);
            IsoTypeWriter.writeUInt32(byteBuffer, bytesPerFrame);
            IsoTypeWriter.writeUInt32(byteBuffer, bytesPerSample);
            byteBuffer.put(soundVersion2Data);
        }
        writableByteChannel.write((ByteBuffer) ((Buffer)byteBuffer).rewind());
        writeContainer(writableByteChannel);
    }

    @Override
    public long getSize() {
        long s = 28
                + (soundVersion == 1 ? 16 : 0)
                + (soundVersion == 2 ? 36 : 0) + getContainerSize();
        s += ((this.largeBox || (s + 8) >= (1L << 32)) ? 16 : 8);
        return s;

    }

    @Override
    public String toString() {
        return "AudioSampleEntry{" +
                "bytesPerSample=" + bytesPerSample +
                ", bytesPerFrame=" + bytesPerFrame +
                ", bytesPerPacket=" + bytesPerPacket +
                ", samplesPerPacket=" + samplesPerPacket +
                ", packetSize=" + packetSize +
                ", compressionId=" + compressionId +
                ", soundVersion=" + soundVersion +
                ", sampleRate=" + sampleRate +
                ", sampleSize=" + sampleSize +
                ", channelCount=" + channelCount +
                ", boxes=" + getBoxes() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AudioSampleEntry that = (AudioSampleEntry) o;
        ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
        ByteArrayOutputStream  baos2 = new ByteArrayOutputStream();
        try {
            this.getBox(Channels.newChannel(baos1));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            that.getBox(Channels.newChannel(baos2));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return Arrays.equals(baos1.toByteArray(), baos2.toByteArray());
    }

    @Override
    public int hashCode() {
        ByteArrayOutputStream  baos1 = new ByteArrayOutputStream();
        try {
            this.getBox(Channels.newChannel(baos1));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Arrays.hashCode(baos1.toByteArray());
    }

}
