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
import com.coremedia.iso.boxes.BoxInterface;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Contains information common to all visual tracks.
 * <code>
 * <pre>
 * class VisualSampleEntry(codingname) extends SampleEntry (codingname){
 * unsigned int(16) pre_defined = 0;
 * const unsigned int(16) reserved = 0;
 * unsigned int(32)[3] pre_defined = 0;
 * unsigned int(16) width;
 * unsigned int(16) height;
 * template unsigned int(32) horizresolution = 0x00480000; // 72 dpi
 * template unsigned int(32) vertresolution = 0x00480000; // 72 dpi
 * const unsigned int(32) reserved = 0;
 * template unsigned int(16) frame_count = 1;
 * string[32] compressorname;
 * template unsigned int(16) depth = 0x0018;
 * int(16) pre_defined = -1;
 * }<br>
 * </pre>
 * </code>
 * <p/>
 * Format-specific informationis appened as boxes after the data described in ISO/IEC 14496-12 chapter 8.16.2.
 */
public class VisualSampleEntry extends SampleEntry implements ContainerBox {
    public static final String TYPE1 = "mp4v";
    public static final String TYPE2 = "s263";
    public static final String TYPE3 = "avc1";
    public static final String TYPE4 = "ovc1";


    /**
     * Identifier for an encrypted video track.
     *
     * @see com.coremedia.iso.boxes.ProtectionSchemeInformationBox
     */
    public static final String TYPE_ENCRYPTED = "encv";


    private int width;
    private int height;
    private double horizresolution;
    private double vertresolution;
    private int frameCount;
    private String compressorname;
    private int depth;

    private long[] predefined = new long[3];

    //VC-1 sample entries don't seem to be spec compliant - so we just copy the content
    private byte[] vc1Content;

    public VisualSampleEntry(byte[] type) {
        super(type);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getHorizresolution() {
        return horizresolution;
    }

    public double getVertresolution() {
        return vertresolution;
    }

    public int getFrameCount() {
        return frameCount;
    }

    public String getCompressorname() {
        return compressorname;
    }

    public int getDepth() {
        return depth;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        if (TYPE4.equals(IsoFile.bytesToFourCC(type))) {
            byte[] vc1 = new byte[(int) size - 8]; //substract reserved and dataReferenceIndex (see super#parse)
            in.read(vc1);
            vc1Content = vc1;
        } else {
            long tmp = in.readUInt16();
            assert 0 == tmp : "reserved byte not 0";
            tmp = in.readUInt16();
            assert 0 == tmp : "reserved byte not 0";
            predefined[0] = in.readUInt32();     // should be zero
            predefined[1] = in.readUInt32();     // should be zero
            predefined[2] = in.readUInt32();     // should be zero
            width = in.readUInt16();
            height = in.readUInt16();
            horizresolution = in.readFixedPoint1616();
            vertresolution = in.readFixedPoint1616();
            tmp = in.readUInt32();
            assert 0 == tmp : "reserved byte not 0";
            frameCount = in.readUInt16();
            int compressornameLength = in.readUInt8();
            byte[] bytes = in.read(compressornameLength);
            compressorname = new String(bytes, "UTF-8");
            if (compressornameLength < 31) {
                byte[] zeros = in.read(31 - compressornameLength);
                assert Arrays.equals(zeros, new byte[zeros.length]) : "The compressor name length was not filled up with zeros";
            }
            depth = in.readUInt16();
            tmp = in.readUInt16();
            assert 0xFFFF == tmp;

            size -= 78;
            ArrayList<BoxInterface> someBoxes = new ArrayList<BoxInterface>();
            while (size > 8) { // If there are just some stupid dead bytes don't try to make a new box
                BoxInterface b = boxParser.parseBox(in, this, lastMovieFragmentBox);
                someBoxes.add(b);
                size -= b.getSize();
            }
            boxes = someBoxes.toArray(new Box[someBoxes.size()]);
            // commented out since it forbids deadbytes
            //  assert size == 0 : "After parsing all boxes there are " + size + " bytes left. 0 bytes required";
        }
    }


    @SuppressWarnings("unchecked")
    public <T extends BoxInterface> T[] getBoxes(Class<T> clazz) {
        ArrayList<T> boxesToBeReturned = new ArrayList<T>();
        for (BoxInterface boxe : boxes) {
            if (clazz.isInstance(boxe)) {
                boxesToBeReturned.add(clazz.cast(boxe));
            }
        }
        return boxesToBeReturned.toArray((T[]) Array.newInstance(clazz, boxesToBeReturned.size()));
    }

    public BoxInterface[] getBoxes() {
        return boxes;
    }

    protected long getContentSize() {
        if (TYPE4.equals(IsoFile.bytesToFourCC(type))) {
            return vc1Content.length + 8;
        }
        long contentSize = 78;
        for (BoxInterface boxe : boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }


    public String getDisplayName() {
        return "Visual Sample Entry";
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        if (TYPE4.equals(IsoFile.bytesToFourCC(type))) {
            isos.write(new byte[6]);
            isos.writeUInt16(getDataReferenceIndex());
            isos.write(vc1Content);
        } else {
            isos.write(new byte[6]);
            isos.writeUInt16(getDataReferenceIndex());
            isos.writeUInt16(0);
            isos.writeUInt16(0);
            isos.writeUInt32(predefined[0]);
            isos.writeUInt32(predefined[1]);
            isos.writeUInt32(predefined[2]);

            isos.writeUInt16(getWidth());
            isos.writeUInt16(getHeight());

            isos.writeFixedPont1616(getHorizresolution());
            isos.writeFixedPont1616(getVertresolution());

            isos.writeUInt32(0);
            isos.writeUInt16(getFrameCount());
            isos.writeUInt8(utf8StringLengthInBytes(getCompressorname()));
            isos.writeStringNoTerm(getCompressorname());
            int a = utf8StringLengthInBytes(getCompressorname());
            while (a < 31) {
                a++;
                isos.write(0);
            }
            isos.writeUInt16(getDepth());
            isos.writeUInt16(0xFFFF);
            for (BoxInterface boxe : boxes) {
                boxe.getBox(isos);
            }
        }
    }

}
