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

package com.coremedia.iso.boxes.odf;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.BoxInterface;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * The ContentID box (‘ccid’) contains the unique identifier for the Content Object the metadata are associated with.
 * The value of the ContentID MUST be the value of the ContentID stored in the Common Headers for this Content Object.
 * There MUST be exactly one ContentID sub-box per User-Data box, as the first sub-box in the container.
 */
public class OmaDrmContentIdBox extends AbstractFullBox {
    public static final String TYPE = "ccid";
    String contentId;

    public OmaDrmContentIdBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    protected long getContentSize() {
        try {
            return 2 + contentId.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getDisplayName() {
        return "Content Id Sub Box";
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt16(contentId.getBytes("UTF-8").length);
        os.write(contentId.getBytes("UTF-8"));
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        int length = in.readUInt16();
        contentId = in.readString(length);
    }
}
