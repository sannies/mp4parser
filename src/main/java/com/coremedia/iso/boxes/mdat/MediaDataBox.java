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

package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * This box contains the media data. In video tracks, this box would contain video frames. A presentation may
 * contain zero or more Media Data Boxes. The actual media data follows the type field; its structure is described
 * by the metadata (see {@link com.coremedia.iso.boxes.SampleTableBox}).<br>
 * In large presentations, it may be desirable to have more data in this box than a 32-bit size would permit. In this
 * case, the large variant of the size field is used.<br>
 * There may be any number of these boxes in the file (including zero, if all the media data is in other files). The
 * metadata refers to media data by its absolute offset within the file (see {@link com.coremedia.iso.boxes.StaticChunkOffsetBox});
 * so Media Data Box headers and free space may easily be skipped, and files without any box structure may
 * also be referenced and used.
 */
public final class MediaDataBox extends AbstractBox {
    public static final String TYPE = "mdat";


    private byte[] deadBytesBefore = new byte[0];

    private IsoBufferWrapper isoBufferWrapper;

    public MediaDataBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public byte[] getDeadBytesBefore() {
        return deadBytesBefore;
    }


    @Override
    public void getBox(IsoOutputStream os) throws IOException {
        os.write(getHeader());
        os.write(getDeadBytesBefore());
        getContent(os);
        if (deadBytes != null) {
            deadBytes.position(0);
            while (deadBytes.remaining() > 0) {
                os.write(deadBytes.read());
            }
        }

    }

    @Override
    public long getSize() {
        long contentSize = getContentSize();  // avoid calling getContentSize() twice
        long headerSize = 4 + // headerSize
                4 + // type
                (contentSize >= 4294967296L ? 8 : 0);
        return headerSize + contentSize + (deadBytes == null ? 0 : deadBytes.size()) + getDeadBytesBefore().length;
    }


    @Override
    protected long getContentSize() {
        return isoBufferWrapper.size();
    }

    @Override
    public void parse(final IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        long start = in.position();
        this.isoBufferWrapper = in.getSegment(start, size);
        in.position(start);
        in.skip(size);
    }


    @Override
    public String getDisplayName() {
        return "Media Data Box";
    }


    @Override
    protected void getContent(IsoOutputStream os) throws IOException {

        isoBufferWrapper.position(0);

        while (isoBufferWrapper.remaining() > 1024) {
            byte[] buf = new byte[1024];
            isoBufferWrapper.read(buf);
            os.write(buf);
        }
        while (isoBufferWrapper.remaining() > 0) {
            os.write(isoBufferWrapper.read());
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("MediaDataBox");
        sb.append("{offset=").append(getOffset());
        sb.append(", size=").append(getSize());
        sb.append('}');
        return sb.toString();
    }

}
