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


import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * aligned(8) class ItemLocationBox extends FullBox(‘iloc’, version, 0) {
 * unsigned int(4) offset_size;
 * unsigned int(4) length_size;
 * unsigned int(4) base_offset_size;
 * if (version == 1)
 * unsigned int(4) index_size;
 * else
 * unsigned int(4) reserved;
 * unsigned int(16) item_count;
 * for (i=0; i<item_count; i++) {
 * unsigned int(16) item_ID;
 * if (version == 1) {
 * unsigned int(12) reserved = 0;
 * unsigned int(4) construction_method;
 * }
 * unsigned int(16) data_reference_index;
 * unsigned int(base_offset_size*8) base_offset;
 * unsigned int(16) extent_count;
 * for (j=0; j<extent_count; j++) {
 * if ((version == 1) && (index_size > 0)) {
 * unsigned int(index_size*8) extent_index;
 * }
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
    public int indexSize;
    public int itemCount;
    public Item[] items;

    public static final String TYPE = "iloc";

    public ItemLocationBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        long size = 6 + (getVersion() != 1 ? 2 : 0);
        for (Item item : items) {
            size += item.getContentSize();
        }
        return size;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        int tmp = IsoTypeReader.readUInt8(content);
        offsetSize = tmp >>> 4;
        lengthSize = tmp & 0xf;
        tmp = IsoTypeReader.readUInt8(content);
        baseOffsetSize = tmp >>> 4;

        if (getVersion() == 1) {
            indexSize = tmp & 0xf;
        } else {
            itemCount = IsoTypeReader.readUInt16(content);
        }

        items = new Item[itemCount];
        for (int i = 0; i < items.length; i++) {
            items[i] = new Item(content);
        }
    }


    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        IsoTypeWriter.writeUInt8(bb, ((offsetSize << 4) | lengthSize));
        if (getVersion() == 1) {
            IsoTypeWriter.writeUInt8(bb, (baseOffsetSize << 4 | indexSize));
        } else {
            IsoTypeWriter.writeUInt8(bb, (baseOffsetSize << 4));
            IsoTypeWriter.writeUInt16(bb, itemCount);
        }

        for (Item item : items) {
            item.getContent(bb);
        }
    }

    public class Item {
        public int itemId;
        public int constructionMethod;
        public int dataReferenceIndex;
        public byte[] baseOffset = new byte[(baseOffsetSize)];
        public int extentCount;
        public Extent[] extents;

        public Item(ByteBuffer in) {
            itemId = IsoTypeReader.readUInt16(in);

            if (getVersion() == 1) {
                int tmp = IsoTypeReader.readUInt16(in);
                constructionMethod = tmp & 0xf;
            }

            dataReferenceIndex = IsoTypeReader.readUInt16(in);
            in.get(baseOffset);
            extentCount = IsoTypeReader.readUInt16(in);
            extents = new Extent[extentCount];

            for (int i = 0; i < extents.length; i++) {
                extents[i] = new Extent(in);
            }
        }

        public long getContentSize() {
            long size = 2 + (getVersion() == 1 ? 2 : 0) + 2 + baseOffsetSize + 2;

            //add extent sizes
            if ((getVersion() == 1) && getIndexSize() > 0) {
                size += extentCount * getIndexSize();
            }
            size += extentCount * (offsetSize + lengthSize);

            return size;
        }

        public void getContent(ByteBuffer bb) throws IOException {
            IsoTypeWriter.writeUInt16(bb, itemId);

            if (getVersion() == 1) {
                IsoTypeWriter.writeUInt16(bb, constructionMethod);
            }

            IsoTypeWriter.writeUInt16(bb, dataReferenceIndex);
            IsoTypeWriter.writeUInt16(bb, extentCount);

            for (Extent extent : extents) {
                extent.getContent(bb);
            }
        }

        public class Extent {
            public byte[] extentOffset;
            public byte[] extentLength;
            public byte[] extentIndex;


            public Extent(ByteBuffer in) {
                if ((getVersion() == 1) && getIndexSize() > 0) {
                    extentIndex = new byte[getIndexSize()];
                    in.get(extentIndex);
                }
                extentOffset = new byte[offsetSize];
                extentLength = new byte[lengthSize];
                in.get(extentOffset);
                in.get(extentLength);
            }

            public void getContent(ByteBuffer os) throws IOException {
                if ((getVersion() == 1) && getIndexSize() > 0) {
                    os.put(extentIndex);
                }
                os.put(extentOffset);
                os.put(extentLength);
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

    public int getIndexSize() {
        return indexSize;
    }

    public void setIndexSize(int indexSize) {
        this.indexSize = indexSize;
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
