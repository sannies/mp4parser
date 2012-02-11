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

package com.coremedia.iso.boxes;


import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * aligned(8) class ItemLocationBox extends FullBox(‘iloc’, version, 0) {
unsigned int(4) offset_size;
unsigned int(4) length_size;
unsigned int(4) base_offset_size;
if (version == 1)
unsigned int(4) index_size;
else
unsigned int(4) reserved;
unsigned int(16) item_count;
for (i=0; i<item_count; i++) {
unsigned int(16) item_ID;
if (version == 1) {
unsigned int(12) reserved = 0;
unsigned int(4) construction_method;
}
unsigned int(16) data_reference_index;
unsigned int(base_offset_size*8) base_offset;
unsigned int(16) extent_count;
for (j=0; j<extent_count; j++) {
if ((version == 1) && (index_size > 0)) {
unsigned int(index_size*8) extent_index;
}
unsigned int(offset_size*8) extent_offset;
unsigned int(length_size*8) extent_length;
}
}
}
 *
 */
public class ItemLocationBox extends AbstractFullBox {
    public int offsetSize;
    public int lengthSize;
    public int baseOffsetSize;
    public int indexSize;
    public Item[] items;

    public static final String TYPE = "iloc";

    public ItemLocationBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    @Override
    protected long getContentSize() {
        long size = 4;
        for (Item item : items) {
            size += item.getContentSize();
        }
        return size;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);

        int tmp = in.readUInt8();
        offsetSize = tmp >>> 4;
        lengthSize = tmp & 0xf;
        tmp = in.readUInt8();
        baseOffsetSize = tmp >>> 4;

        if (getVersion() == 1) {
            indexSize = tmp & 0xf;
        }

        int itemCount = in.readUInt16();
        items = new Item[itemCount];
        for (int i = 0; i < items.length; i++) {
            items[i] = new Item(in);
        }
    }


    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt8(((offsetSize << 4) | lengthSize));
        if (getVersion() == 1) {
            os.writeUInt8((baseOffsetSize << 4 | indexSize));
        } else {
            os.writeUInt8((baseOffsetSize << 4));
        }

        os.writeUInt16(items.length);

        for (Item item : items) {
            item.getContent(os);
        }
    }

    public static long toLong(byte[] bytes) {
      return ((ByteBuffer) ByteBuffer.allocate(8).position(Math.max(0, 8 - bytes.length))).put(bytes, 0, Math.min(8, bytes.length)).getLong(0);
    }

    public Item createItem(int itemId, int constructionMethod, int dataReferenceIndex, byte[] baseOffset, Extent[] extents) {
        return new Item(itemId, constructionMethod, dataReferenceIndex, baseOffset, extents);
    }

    public Extent createExtent(byte[] extentOffset, byte[] extentLength, byte[] extentIndex) {
        return new Extent(extentOffset, extentLength, extentIndex);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("ItemLocationBox");
        sb.append("{offsetSize=").append(offsetSize);
        sb.append(", lengthSize=").append(lengthSize);
        sb.append(", baseOffsetSize=").append(baseOffsetSize);
        sb.append(", indexSize=").append(indexSize);
        sb.append(", items=").append(items == null ? "null" : Arrays.asList(items).toString());
        sb.append('}');
        return sb.toString();
    }

    public int getOffsetSize() {
        return offsetSize;
    }

    public void setOffsetSize(int offsetSize) {
        this.offsetSize = offsetSize;
    }

    public int getLengthSize() {
        return lengthSize;
    }

    public void setLengthSize(int lengthSize) {
        this.lengthSize = lengthSize;
    }

    public int getBaseOffsetSize() {
        return baseOffsetSize;
    }

    public void setBaseOffsetSize(int baseOffsetSize) {
        this.baseOffsetSize = baseOffsetSize;
    }

    public int getIndexSize() {
        return indexSize;
    }

    public void setIndexSize(int indexSize) {
        this.indexSize = indexSize;
    }

    public int getItemCount() {
        return items.length;
    }

    public Item[] getItems() {
        return items;
    }

    public void setItems(Item[] items) {
        this.items = items;
    }

    public class Item {
        public int itemId;
        public int constructionMethod;
        public int dataReferenceIndex;
        public byte[] baseOffset = new byte[(baseOffsetSize)];
        public Extent[] extents;

        public int getExtentCount() {
            return extents.length;
        }

        public void setBaseOffsetAdjustingExtents(long offsetToData) {
            this.baseOffset = toByteArray(offsetToData, baseOffsetSize);
            int base = 0;
            for (Extent extent : extents) {
                extent.extentOffset = toByteArray(base + offsetToData, offsetSize);
                base += toLong(extent.extentLength);
            }
        }

        public Item(int itemId, int constructionMethod, int dataReferenceIndex, byte[] baseOffset, Extent[] extents) {
            this.itemId = itemId;
            this.constructionMethod = constructionMethod;
            this.dataReferenceIndex = dataReferenceIndex;
            this.baseOffset = baseOffset;
            this.extents = extents;
        }

        public Item(IsoBufferWrapper in) throws IOException {
            itemId = in.readUInt16();

            if (getVersion() == 1) {
                int tmp = in.readUInt16();
                constructionMethod = tmp & 0xf;
            }

            dataReferenceIndex = in.readUInt16();
            in.read(baseOffset);
            int extentCount = in.readUInt16();
            extents = new Extent[extentCount];

            for (int i = 0; i < extents.length; i++) {
                extents[i] = new Extent(in);
            }
        }

        public long getContentSize() {
            long size = 2 + (getVersion() == 1 ? 2 : 0) + 2 + baseOffsetSize + 2;

            //add extent sizes
            if ((getVersion() == 1) && getIndexSize() > 0) {
                size += extents.length * getIndexSize();
            }
            size += extents.length * (offsetSize + lengthSize);

            return size;
        }

        public void getContent(IsoOutputStream os) throws IOException {
            os.writeUInt16(itemId);

            if (getVersion() == 1) {
                os.writeUInt16(constructionMethod);
            }

            os.writeUInt16(dataReferenceIndex);
            os.write(baseOffset);
            os.writeUInt16(extents.length);

            for (Extent extent : extents) {
                extent.getContent(os);
            }
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Item");
            sb.append("{itemId=").append(itemId);
            sb.append(", constructionMethod=").append(constructionMethod);
            sb.append(", dataReferenceIndex=").append(dataReferenceIndex);
            sb.append(", baseOffset=").append(baseOffset == null ? "null" : toLong(baseOffset));
            sb.append(", extentCount=").append(extents == null ? 0 : extents.length);
            sb.append(", extents=").append(extents == null ? "null" : Arrays.asList(extents).toString());
            sb.append('}');
            return sb.toString();
        }
    }

    public class Extent {
        public byte[] extentOffset;
        public byte[] extentLength;
        public byte[] extentIndex;

        public Extent(byte[] extentOffset, byte[] extentLength, byte[] extentIndex) {
            this.extentOffset = extentOffset;
            this.extentLength = extentLength;
            this.extentIndex = extentIndex;
        }

        public Extent(IsoBufferWrapper in) throws IOException {
            if ((getVersion() == 1) && getIndexSize() > 0) {
                extentIndex = new byte[getIndexSize()];
                in.read(extentIndex);
            }
            extentOffset = new byte[offsetSize];
            extentLength = new byte[lengthSize];
            in.read(extentOffset);
            in.read(extentLength);
        }

        public void getContent(IsoOutputStream os) throws IOException {
            if ((getVersion() == 1) && getIndexSize() > 0) {
                os.write(extentIndex);
            }
            os.write(extentOffset);
            os.write(extentLength);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Extent");
            sb.append("{extentOffset=").append(extentOffset == null ? "null" : toLong(extentOffset));
            sb.append(", extentLength=").append(extentLength == null ? "null" : toLong(extentLength));
            sb.append(", extentIndex=").append(extentIndex == null ? "null" : toLong(extentIndex));
            sb.append('}');
            return sb.toString();
        }
    }

    public static byte[] toByteArray(long number, int bytes) {
      ByteBuffer result = ByteBuffer.allocate(bytes);
      //todo: how does casting overflow?
      switch (bytes) {
        case 1:
          result.put((byte) number);
          break;
        case 2:
          result.putShort((short) number);
          break;
        case 4:
          result.putInt((int) number);
          break;
        case 8:
          result.putLong(number);
          break;
        default:
          throw new NumberFormatException();
      }
      return result.array();
    }
}
