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

/**
 * aligned(8) class ItemLocationBox extends FullBox(‘iloc’, version = 0, 0) {
 * unsigned int(4) offset_size;
 * unsigned int(4) length_size;
 * unsigned int(4) base_offset_size;
 * unsigned int(4) reserved;
 * unsigned int(16) item_count;
 * for (i=0; i<item_count; i++) {
 * unsigned int(16) item_ID;
 * unsigned int(16) data_reference_index;
 * unsigned int(base_offset_size*8) base_offset;
 * unsigned int(16) extent_count;
 * for (j=0; j<extent_count; j++) {
 * unsigned int(offset_size*8) extent_offset;
 * unsigned int(length_size*8) extent_length;
 * }
 * }
 * }
 */
public class ItemLocationBox extends AbstractFullBox {
    public int offsetSize;
    public int lengthSize;
    public int baseOffsetSize;
    public int itemCount;
    public Item[] items;

    public static final String TYPE = "iloc";

    public ItemLocationBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    @Override
    protected long getContentSize() {
        long size = 4 + itemCount * (6 + baseOffsetSize * 8);
        for (Item item : items) {
            size += item.getContentSize();
        }
        return size;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        int tmp = in.readUInt8();
        offsetSize = tmp >>> 4;
        lengthSize = tmp & 0xf;
        tmp = in.readUInt8();
        baseOffsetSize = tmp >>> 4;

        itemCount = in.readUInt16();
        items = new Item[itemCount];
        for (int i = 0; i < items.length; i++) {
            items[i] = new Item(in);
        }
    }


    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt8(((offsetSize << 4) | lengthSize));
        os.writeUInt8((baseOffsetSize << 4));
        os.writeUInt16(itemCount);

        for (Item item : items) {
            item.getContent(os);
        }
    }

    public class Item {
        public int itemId;
        public int dataReferenceIndex;
        public byte[] baseOffset = new byte[(baseOffsetSize * 8)];
        public int extentCount;
        public Extent[] extents;

        public Item(IsoBufferWrapper in) throws IOException {
            itemId = in.readUInt16();
            dataReferenceIndex = in.readUInt16();
            in.read(baseOffset);
            extentCount = in.readUInt16();
            extents = new Extent[extentCount];

            for (int i = 0; i < extents.length; i++) {
                extents[i] = new Extent(in);
            }
        }

        public int getContentSize() {
            return extentCount * (offsetSize + lengthSize);
        }

        public void getContent(IsoOutputStream os) throws IOException {
            os.writeUInt16(itemId);
            os.writeUInt16(dataReferenceIndex);
            os.writeUInt16(extentCount);

            for (Extent extent : extents) {
                extent.getContent(os);
            }
        }

        public class Extent {
            public byte[] extentOffset;
            public byte[] extentLength;


            public Extent(IsoBufferWrapper in) throws IOException {
                extentOffset = new byte[offsetSize];
                extentOffset = new byte[lengthSize];
                in.read(extentOffset);
                in.read(extentLength);
            }

            public void getContent(IsoOutputStream os) throws IOException {
                os.write(extentOffset);
                os.write(extentLength);
            }
        }
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

    public int getItemCount() {
        return itemCount;
    }

    public void setItemCount(int itemCount) {
        this.itemCount = itemCount;
    }

    public Item[] getItems() {
        return items;
    }

    public void setItems(Item[] items) {
        this.items = items;
    }
}
