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

package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Contains basic information about the audio samples in this track. Format-specific information
 * is appened as boxes after the data described in ISO/IEC 14496-12 chapter 8.16.2.
 */
public class AudioSampleEntry extends SampleEntry implements ContainerBox {

    public static final String TYPE1 = "samr";
    public static final String TYPE2 = "sawb";
    public static final String TYPE3 = "mp4a";
    public static final String TYPE4 = "drms";
    public static final String TYPE5 = "alac";
    public static final String TYPE7 = "owma";
    public static final String TYPE8 = "ac-3"; /* ETSI TS 102 366 1.2.1 Annex F */

    /**
     * Identifier for an encrypted audio track.
     *
     * @see com.coremedia.iso.boxes.ProtectionSchemeInformationBox
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

    public AudioSampleEntry(byte[] type) {
        super(type);
    }

    public int getChannelCount() {
        return channelCount;
    }

    public int getSampleSize() {
        return sampleSize;
    }

    public long getSampleRate() {
        return sampleRate;
    }

    public int getSoundVersion() {
        return soundVersion;
    }

    public int getCompressionId() {
        return compressionId;
    }

    public int getPacketSize() {
        return packetSize;
    }

    public long getSamplesPerPacket() {
        return samplesPerPacket;
    }

    public long getBytesPerPacket() {
        return bytesPerPacket;
    }

    public long getBytesPerFrame() {
        return bytesPerFrame;
    }

    public long getBytesPerSample() {
        return bytesPerSample;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        // 8 bytes already parsed
        //reserved bits - used by qt
        soundVersion = in.readUInt16();

        //reserved
        reserved1 = in.readUInt16();
        reserved2 = in.readUInt32();

        channelCount = in.readUInt16();
        sampleSize = in.readUInt16();
        //reserved bits - used by qt
        compressionId = in.readUInt16();
        //reserved bits - used by qt
        packetSize = in.readUInt16();
        //sampleRate = in.readFixedPoint1616();
        sampleRate = in.readUInt32() >>> 16;

        //more qt stuff - see http://mp4v2.googlecode.com/svn-history/r388/trunk/src/atom_sound.cpp
        if (soundVersion > 0) {
            samplesPerPacket = in.readUInt32();
            bytesPerPacket = in.readUInt32();
            bytesPerFrame = in.readUInt32();
            bytesPerSample = in.readUInt32();
            size -= 16;
        }
        if (soundVersion == 2) {
            soundVersion2Data = in.read(20);
            size -=20;
        }
        size -= 28;
        while (size > 8) {
            if (TYPE7.equals(IsoFile.bytesToFourCC(type))) {
                //microsoft garbage
                break;
            }
            Box b = boxParser.parseBox(in, this, lastMovieFragmentBox);
            boxes.add(b);
            size -= b.getSize();
        }
        // commented out since it forbids deadbytes
        //assert size == 0 : "After parsing all boxes there are " + size + " bytes left. 0 bytes required";
    }


    @Override
    protected long getContentSize() {
        long contentSize = 28;
        contentSize += soundVersion>0?16:0;
        contentSize += soundVersion==2?20:0;
        for (Box boxe : boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    @Override
    public String getDisplayName() {
        return "Audio Sample Entry";
    }

    public String toString() {
        return "AudioSampleEntry";
    }

    @Override
    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.write(new byte[6]);
        isos.writeUInt16(getDataReferenceIndex());
        isos.writeUInt16(soundVersion);
        isos.writeUInt16(reserved1);
        isos.writeUInt32(reserved2);
        isos.writeUInt16(getChannelCount());
        isos.writeUInt16(getSampleSize());
        isos.writeUInt16(0);
        isos.writeUInt16(0);
        //isos.writeFixedPont1616(getSampleRate());
        isos.writeUInt32(getSampleRate() << 16);
        if (soundVersion > 0) {
            isos.writeUInt32(samplesPerPacket);
            isos.writeUInt32(bytesPerPacket);
            isos.writeUInt32(bytesPerFrame);
            isos.writeUInt32(bytesPerSample);
        }

        if (soundVersion == 2) {
            isos.write(soundVersion2Data);
        }

        for (Box boxe : boxes) {
            boxe.getBox(isos);
        }
    }
}
