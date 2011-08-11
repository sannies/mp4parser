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
import java.util.logging.Logger;

/**
 * A box with no internal structure. We read it and write it without looking into the content.
 */
public abstract class LiteralBox extends AbstractBox {
    private static Logger LOG = Logger.getLogger(LiteralBox.class.getName());

    byte[] content;

    public LiteralBox(byte[] type) {
        super(type);
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        if (size == -1) { // length = rest of file!
            throw new IOException("box size of -1 is not supported. Boxsize -1 means box reaches until the end of the file.");
        } else if (((int) size) != size) {
            throw new IOException("The UnknownBox cannot be larger than 2^32 bytes(Plz enhance parser!!)");
        } else {
            content = in.read((int) size);
        }
    }

    protected long getContentSize() {
        return content.length;
    }

    public String toString() {
        return "UnknownBox[type=" + IsoFile.bytesToFourCC(getType()) + ";contentLength=" + (content != null ? content.length : "?") + "]";
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.write(content);
    }

    public void setContent(byte[] content) {
        this.content = content;
    }
}
