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
 * List of keywords according to 3GPP 26.244.
 */
public class KeywordsBox extends AbstractFullBox {
    public static final String TYPE = "kywd";

    private String language;
    private String[] keywords;

    public KeywordsBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getLanguage() {
        return language;
    }

    public String[] getKeywords() {
        return keywords;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setKeywords(String[] keywords) {
        this.keywords = keywords;
    }

    protected long getContentSize() {
        long contentSize = 2 + 1;
        for (String keyword : keywords) {
            contentSize += 1 + utf8StringLengthInBytes(keyword) + 1;
        }
        return contentSize;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        language = in.readIso639();
        int keywordCount = in.readUInt8();
        keywords = new String[keywordCount];
        for (int i = 0; i < keywordCount; i++) {
            in.readUInt8();
            keywords[i] = in.readString();
        }
    }

    protected void getContent(IsoOutputStream isos) throws IOException {

        isos.writeIso639(language);
        isos.writeUInt8(keywords.length);
        for (String keyword : keywords) {
            isos.writeUInt8(utf8StringLengthInBytes(keyword) + 1);
            isos.writeStringZeroTerm(keyword);
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("KeywordsBox[language=").append(getLanguage());
        for (int i = 0; i < keywords.length; i++) {
            buffer.append(";keyword").append(i).append("=").append(keywords[i]);
        }
        buffer.append("]");
        return buffer.toString();
    }
}
