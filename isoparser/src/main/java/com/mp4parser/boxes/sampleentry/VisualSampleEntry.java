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

package com.mp4parser.boxes.sampleentry;

import com.mp4parser.BoxParser;
import com.mp4parser.Container;
import com.mp4parser.boxes.iso14496.part12.ProtectionSchemeInformationBox;
import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.IsoTypeWriter;
import com.mp4parser.tools.Utf8;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * <h1>4cc = "{@value #TYPE1}" || "{@value #TYPE2}" || "{@value #TYPE3}" || "{@value #TYPE4}" || "{@value #TYPE5}"</h1>
 * Contains information common to all visual tracks.
 * <pre>
 * class VisualSampleEntry(codingname) extends AbstractSampleEntry (codingname){
 *  unsigned int(16) pre_defined = 0;
 *  const unsigned int(16) reserved = 0;
 *  unsigned int(32)[3] pre_defined = 0;
 *  unsigned int(16) width;
 *  unsigned int(16) height;
 *  template unsigned int(32) horizresolution = 0x00480000; // 72 dpi
 *  template unsigned int(32) vertresolution = 0x00480000; // 72 dpi
 *  const unsigned int(32) reserved = 0;
 *  template unsigned int(16) frame_count = 1;
 *  string[32] compressorname;
 *  template unsigned int(16) depth = 0x0018;
 *  int(16) pre_defined = -1;
 * }
 * </pre>
 *
 * Format-specific information is appened as boxes after the data described in ISO/IEC 14496-12 chapter 8.16.2.
 */
public final class VisualSampleEntry extends AbstractSampleEntry implements Container {
    public static final String TYPE1 = "mp4v";
    public static final String TYPE2 = "s263";
    public static final String TYPE3 = "avc1";
    public static final String TYPE4 = "avc3";
    public static final String TYPE5 = "drmi";
    public static final String TYPE6 = "hvc1";
    public static final String TYPE7 = "hev1";


    /**
     * Identifier for an encrypted video track.
     *
     * @see ProtectionSchemeInformationBox
     */
    public static final String TYPE_ENCRYPTED = "encv";


    private int width;
    private int height;
    private double horizresolution = 72;
    private double vertresolution = 72;
    private int frameCount = 1;
    private String compressorname = "";
    private int depth = 24;

    private long[] predefined = new long[3];

    public VisualSampleEntry() {
        super(TYPE3);
    }

    public VisualSampleEntry(String type) {
        super(type);
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public double getHorizresolution() {
        return horizresolution;
    }

    public void setHorizresolution(double horizresolution) {
        this.horizresolution = horizresolution;
    }

    public double getVertresolution() {
        return vertresolution;
    }

    public void setVertresolution(double vertresolution) {
        this.vertresolution = vertresolution;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public void setFrameCount(int frameCount) {
        this.frameCount = frameCount;
    }

    public String getCompressorname() {
        return compressorname;
    }

    public void setCompressorname(String compressorname) {
        this.compressorname = compressorname;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public void parse(final ReadableByteChannel dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {

        ByteBuffer content = ByteBuffer.allocate(78);
        dataSource.read(content);
        content.position(6);
        dataReferenceIndex = IsoTypeReader.readUInt16(content);

        long tmp = IsoTypeReader.readUInt16(content);
        assert 0 == tmp : "reserved byte not 0";
        tmp = IsoTypeReader.readUInt16(content);
        assert 0 == tmp : "reserved byte not 0";
        predefined[0] = IsoTypeReader.readUInt32(content);     // should be zero
        predefined[1] = IsoTypeReader.readUInt32(content);     // should be zero
        predefined[2] = IsoTypeReader.readUInt32(content);     // should be zero
        width = IsoTypeReader.readUInt16(content);
        height = IsoTypeReader.readUInt16(content);
        horizresolution = IsoTypeReader.readFixedPoint1616(content);
        vertresolution = IsoTypeReader.readFixedPoint1616(content);
        tmp = IsoTypeReader.readUInt32(content);
        assert 0 == tmp : "reserved byte not 0";
        frameCount = IsoTypeReader.readUInt16(content);
        int compressornameDisplayAbleData = IsoTypeReader.readUInt8(content);
        if (compressornameDisplayAbleData > 31) {
            //System.out.println("invalid compressor name displayable data: " + compressornameDisplayAbleData);
            compressornameDisplayAbleData = 31;
        }
        byte[] bytes = new byte[compressornameDisplayAbleData];
        content.get(bytes);
        compressorname = Utf8.convert(bytes);
        if (compressornameDisplayAbleData < 31) {
            byte[] zeros = new byte[31 - compressornameDisplayAbleData];
            content.get(zeros);
            //assert Mp4Arrays.equals(zeros, new byte[zeros.length]) : "The compressor name length was not filled up with zeros";
        }
        depth = IsoTypeReader.readUInt16(content);
        tmp = IsoTypeReader.readUInt16(content);
        assert 0xFFFF == tmp;


        initContainer(dataSource, contentSize - 78, boxParser);

    }


    @Override
    public void getBox(WritableByteChannel writableByteChannel) throws IOException {
        writableByteChannel.write(getHeader());
        ByteBuffer byteBuffer = ByteBuffer.allocate(78);
        byteBuffer.position(6);
        IsoTypeWriter.writeUInt16(byteBuffer, dataReferenceIndex);
        IsoTypeWriter.writeUInt16(byteBuffer, 0);
        IsoTypeWriter.writeUInt16(byteBuffer, 0);
        IsoTypeWriter.writeUInt32(byteBuffer, predefined[0]);
        IsoTypeWriter.writeUInt32(byteBuffer, predefined[1]);
        IsoTypeWriter.writeUInt32(byteBuffer, predefined[2]);

        IsoTypeWriter.writeUInt16(byteBuffer, getWidth());
        IsoTypeWriter.writeUInt16(byteBuffer, getHeight());

        IsoTypeWriter.writeFixedPoint1616(byteBuffer, getHorizresolution());
        IsoTypeWriter.writeFixedPoint1616(byteBuffer, getVertresolution());


        IsoTypeWriter.writeUInt32(byteBuffer, 0);
        IsoTypeWriter.writeUInt16(byteBuffer, getFrameCount());
        IsoTypeWriter.writeUInt8(byteBuffer, Utf8.utf8StringLengthInBytes(getCompressorname()));
        byteBuffer.put(Utf8.convert(getCompressorname()));
        int a = Utf8.utf8StringLengthInBytes(getCompressorname());
        while (a < 31) {
            a++;
            byteBuffer.put((byte) 0);
        }
        IsoTypeWriter.writeUInt16(byteBuffer, getDepth());
        IsoTypeWriter.writeUInt16(byteBuffer, 0xFFFF);

        writableByteChannel.write((ByteBuffer) byteBuffer.rewind());

        writeContainer(writableByteChannel);

    }


    @Override
    public long getSize() {
        long s = getContainerSize();
        long t = 78;
        return s + t + ((largeBox || (s + t + 8) >= (1L << 32)) ? 16 : 8);
    }



}

