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
import java.util.List;

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
public class MediaDataBoxWithSamples extends AbstractBox {

    private List<IsoBufferWrapper> samples;

    public MediaDataBoxWithSamples(List<IsoBufferWrapper> samples) {
        super(IsoFile.fourCCtoBytes("mdat"));
        this.samples = samples;
    }

    @Override
    protected long getContentSize() {
        long size = 0;
        for (IsoBufferWrapper sample : samples) {
            size += sample.size();
        }
        return size;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        for (IsoBufferWrapper ibw : samples) {

            while (ibw.remaining() >= 1024) {
                os.write(ibw.read(1024));
            }
            while (ibw.remaining() > 0) {
                os.write(ibw.readByte());
            }

        }
    }

    public List<IsoBufferWrapper> getSamples() {
        return samples;
    }
}