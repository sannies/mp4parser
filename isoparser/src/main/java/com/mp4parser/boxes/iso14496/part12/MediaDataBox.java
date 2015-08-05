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

package com.mp4parser.boxes.iso14496.part12;

import java.nio.ByteBuffer;

import com.mp4parser.support.AbstractBox;
import com.mp4parser.boxes.iso14496.part12.SampleTableBox;
import com.mp4parser.boxes.iso14496.part12.StaticChunkOffsetBox;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * This box contains the media data. In video tracks, this box would contain video frames. A presentation may
 * contain zero or more Media Data Boxes. The actual media data follows the type field; its structure is described
 * by the metadata (see {@link SampleTableBox}).<br>
 * In large presentations, it may be desirable to have more data in this box than a 32-bit size would permit. In this
 * case, the large variant of the size field is used.<br>
 * There may be any number of these boxes in the file (including zero, if all the media data is in other files). The
 * metadata refers to media data by its absolute offset within the file (see {@link StaticChunkOffsetBox});
 * so Media Data Box headers and free space may easily be skipped, and files without any box structure may
 * also be referenced and used.
 */
public final class MediaDataBox extends AbstractBox {
    public static final String TYPE = "mdat";
    ByteBuffer data;

    public MediaDataBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return data.limit();
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        data = content;
        content.position(content.position() + content.remaining());
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        data.rewind();
        byteBuffer.put(data);
    }

    public ByteBuffer getData() {
        return data;
    }

    public void setData(ByteBuffer data) {
        this.data = data;
    }
}
