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
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Entry type for timed text samples defined in the timed text specification (ISO/IEC 14496-17).
 */
public class TextSampleEntry extends SampleEntry {

    public static final String TYPE1 = "tx3g";

    public static final String TYPE_ENCRYPTED = "enct";

/*  class TextSampleEntry() extends SampleEntry ('tx3g') {
    unsigned int(32)  displayFlags;
    signed int(8)     horizontal-justification;
    signed int(8)     vertical-justification;
    unsigned int(8)   background-color-rgba[4];
    BoxRecord         default-text-box;
    StyleRecord       default-style;
    FontTableBox      font-table;
  }
  */

    private long displayFlags; // 32 bits
    private int horizontalJustification; // 8 bit
    private int verticalJustification;  // 8 bit
    private byte[] backgroundColorRgba; // 4 bytes
    private BoxRecord boxRecord;
    private StyleRecord styleRecord;

    public TextSampleEntry(byte[] type) {
        super(type);
    }


    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        displayFlags = in.readUInt32();
        horizontalJustification = in.readUInt8();
        verticalJustification = in.readUInt8();
        backgroundColorRgba = new byte[4];
        backgroundColorRgba[0] = (byte) in.readUInt8();
        backgroundColorRgba[1] = (byte) in.readUInt8();
        backgroundColorRgba[2] = (byte) in.readUInt8();
        backgroundColorRgba[3] = (byte) in.readUInt8();
        size -= 18;

        boxRecord = new BoxRecord();
        boxRecord.parse(in);
        size -= boxRecord.getSize();

        styleRecord = new StyleRecord();
        styleRecord.parse(in);
        size -= styleRecord.getSize();

        LinkedList<Box> ll = new LinkedList<Box>();

        while (size > 0) {
            Box b = boxParser.parseBox(in, this, lastMovieFragmentBox);
            ll.add(b);
            size -= b.getSize();
        }
        boxes = ll.toArray(new Box[ll.size()]);
    }


    @SuppressWarnings("unchecked")
    public <T extends Box> T[] getBoxes(Class<T> clazz) {
        ArrayList<T> boxesToBeReturned = new ArrayList<T>();
        for (Box boxe : boxes) {
            if (clazz.isInstance(boxe)) {
                boxesToBeReturned.add(clazz.cast(boxe));
            }
        }
        return boxesToBeReturned.toArray((T[]) Array.newInstance(clazz, boxesToBeReturned.size()));
    }

    public Box[] getBoxes() {
        return boxes;
    }

    protected long getContentSize() {
        long contentSize = 18;
        contentSize += boxRecord.getSize();
        contentSize += styleRecord.getSize();
        for (Box boxe : boxes) {
            contentSize += boxe.getSize();
        }
        return contentSize;
    }

    public String getDisplayName() {
        return "Text Sample Entry";
    }

    public String toString() {
        return "TextSampleEntry";
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.write(new byte[6]);
        isos.writeUInt16(getDataReferenceIndex());

        isos.writeUInt32(displayFlags);
        isos.writeUInt8(horizontalJustification);
        isos.writeUInt8(verticalJustification);
        isos.writeUInt8(backgroundColorRgba[0]);
        isos.writeUInt8(backgroundColorRgba[1]);
        isos.writeUInt8(backgroundColorRgba[2]);
        isos.writeUInt8(backgroundColorRgba[3]);
        boxRecord.getContent(isos);
        styleRecord.getContent(isos);

        for (Box boxe : boxes) {
            boxe.getBox(isos);
        }
    }

    public BoxRecord getBoxRecord() {
        return boxRecord;
    }

    public void setBoxRecord(BoxRecord boxRecord) {
        this.boxRecord = boxRecord;
    }

    public StyleRecord getStyleRecord() {
        return styleRecord;
    }

    public void setStyleRecord(StyleRecord styleRecord) {
        this.styleRecord = styleRecord;
    }


    public class BoxRecord {
        int top;
        int left;
        int bottom;
        int right;

        public void parse(IsoBufferWrapper in) {
            top = in.readUInt16();
            left = in.readUInt16();
            bottom = in.readUInt16();
            right = in.readUInt16();
        }

        public void getContent(IsoOutputStream isos) throws IOException {
            isos.writeUInt16(top);
            isos.writeUInt16(left);
            isos.writeUInt16(bottom);
            isos.writeUInt16(right);
        }

        public int getSize() {
            return 8;
        }
    }

    /*
    class FontRecord {
	unsigned int(16) 	font-ID;
	unsigned int(8)	font-name-length;
	unsigned int(8)	font[font-name-length];
}
     */
    public class FontRecord {
        int fontId;
        String fontname;

        public void parse(IsoBufferWrapper in) {
            fontId = in.readUInt16();
            int length = in.readUInt8();
            fontname = in.readString(length);
        }

        public void getContent(IsoOutputStream isos) throws IOException {
            isos.writeUInt16(fontId);
            isos.writeUInt8(fontname.length());
            isos.writeStringNoTerm(fontname);
        }

        public int getSize() {
            return utf8StringLengthInBytes(fontname) + 3;
        }

    }

    /*
   aligned(8) class StyleRecord {
   unsigned int(16) 	startChar;
   unsigned int(16)	endChar;
   unsigned int(16)	font-ID;
   unsigned int(8)	face-style-flags;
   unsigned int(8)	font-size;
   unsigned int(8)	text-color-rgba[4];
}
    */
    public class StyleRecord {
        int startChar;
        int endChar;
        int fontId;
        int faceStyleFlags;
        int fontSize;
        byte[] textColor;

        public void parse(IsoBufferWrapper in) {
            startChar = in.readUInt16();
            endChar = in.readUInt16();
            fontId = in.readUInt16();
            faceStyleFlags = in.readUInt8();
            fontSize = in.readUInt8();
            textColor = new byte[4];
            in.read(textColor);
        }


        public void getContent(IsoOutputStream isos) throws IOException {
            isos.writeUInt16(startChar);
            isos.writeUInt16(endChar);
            isos.writeUInt16(fontId);
            isos.writeUInt8(faceStyleFlags);
            isos.writeUInt8(fontSize);
            isos.write(textColor);
        }

        public int getSize() {
            return 12;
        }
    }

    /*
   class FontTableBox() extends Box(‘ftab’) {
   unsigned int(16) entry-count;
   FontRecord	font-entry[entry-count];
}
    */
    public class FontTableBox extends AbstractBox {
        List<FontRecord> fontRecords = new LinkedList<FontRecord>();

        public FontTableBox() {
            super(IsoFile.fourCCtoBytes("ftab"));
        }

        public void parse(IsoBufferWrapper in) {

        }

        public void getContent(IsoOutputStream isos) throws IOException {
            isos.writeUInt16(fontRecords.size());
            for (FontRecord record : fontRecords) {
                record.getContent(isos);
            }
        }

        @Override
        protected long getContentSize() {
            int size = 2;
            for (FontRecord fontRecord : fontRecords) {
                size += fontRecord.getSize();
            }
            return size;
        }

        @Override
        public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
            int numberOfRecords = in.readUInt16();
            for (int i = 0; i < numberOfRecords; i++) {
                FontRecord fr = new FontRecord();
                fr.parse(in);
                fontRecords.add(fr);
            }
        }

        @Override
        public String getDisplayName() {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

    }
}
