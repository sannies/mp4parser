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

package com.coremedia.iso.boxes.vodafone;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.BoxInterface;
import com.coremedia.iso.boxes.FullBox;

import java.io.IOException;

/**
 * Vodafone specific box. Usage unclear.
 */
public class ContentDistributorIdBox extends FullBox {
    public static final String TYPE = "cdis";

    private String language;
    private String contentDistributorId;

    public ContentDistributorIdBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getLanguage() {
        return language;
    }

    public String getContentDistributorId() {
        return contentDistributorId;
    }

    public String getDisplayName() {
        return "Content Distributor ID Box";
    }

    protected long getContentSize() {
        return 2 + utf8StringLengthInBytes(contentDistributorId) + 1;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        language = in.readIso639();
        contentDistributorId = in.readString();
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeIso639(language);
        isos.writeStringZeroTerm(contentDistributorId);
    }

    public String toString() {
        return "ContentDistributorIdBox[language=" + getLanguage() + ";contentDistributorId=" + getContentDistributorId() + "]";
    }
}
