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
 * Meta information in a 'udta' box about a track.
 * Defined in 3GPP 26.244.
 *
 * @see com.coremedia.iso.boxes.UserDataBox
 */
public class AuthorBox extends FullBox {
    public static final String TYPE = "auth";

    private String language;
    private String author;

    public AuthorBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    /**
     * Declares the language code for the {@link #getAuthor()} return value. See ISO 639-2/T for the set of three
     * character codes.Each character is packed as the difference between its ASCII value and 0x60. The code is
     * confined to being three lower-case letters, so these values are strictly positive.
     *
     * @return the language code
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Author information.
     *
     * @return the author
     */
    public String getAuthor() {
        return author;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDisplayName() {
        return "Author Box";
    }

    protected long getContentSize() {
        return 2 + utf8StringLengthInBytes(author) + 1;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        language = in.readIso639();
        author = in.readString();
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeIso639(language);
        isos.writeStringZeroTerm(author);
    }


    public String toString() {
        return "AuthorBox[language=" + getLanguage() + ";author=" + getAuthor() + "]";
    }
}
