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
 * The contents of a free-space box are irrelevant and may be ignored, or the object deleted, without affecting the
 * presentation. Care should be excercized when deleting the object, as this may invalidate the offsets used in the
 * sample table.
 *
 * @see com.coremedia.iso.boxes.SampleTableBox
 */
public class FreeSpaceBox extends Box {
    public static final String TYPE = "skip";

    byte[] content;

    protected long getContentSize() {
        return content.length;
    }

    public FreeSpaceBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        if (((int) size) != size) {
            throw new RuntimeException("The FreeSpaceBox cannot be larger than 2^32 bytes!");
        }
        content = in.read((int) size);

    }

    public void setData(byte[] data) {
        this.content = data;
    }

    public byte[] getData() {
        return content;
    }


    protected void getContent(IsoOutputStream os) throws IOException {
        os.write(content);
    }

    public String getDisplayName() {
        return "Free Space Box";
    }

    public String toString() {
        return "FreeSpaceBox[size=" + content.length + ";type=" + IsoFile.bytesToFourCC(getType()) + "]";
    }
}
