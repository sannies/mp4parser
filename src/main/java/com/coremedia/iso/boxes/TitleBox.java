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
import java.io.UnsupportedEncodingException;

/**
 * <code>
 * Box Type: 'titl'<br>
 * Container: {@link UserDataBox} ('udta')<br>
 * Mandatory: No<br>
 * Quantity: Zero or one<br><br>
 * </code>
 * <p/>
 * Title for the media.
 */
public class TitleBox extends AbstractFullBox {
    public static final String TYPE = "titl";

    private String language;
    private String title;

    public TitleBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getLanguage() {
        return language;
    }

    public String getTitle() {
        return title;
    }

    /**
     * Sets the 3-letter ISO-639 language for this title.
     *
     * @param language 3-letter ISO-639 code
     */
    public void setLanguage(String language) {
        this.language = language;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDisplayName() {
        return "Title Box";
    }

    protected long getContentSize() {
        try {
            return 3 + title.getBytes("UTF-8").length;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
    }


    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeIso639(language);
        isos.writeStringZeroTerm(title);
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        language = in.readIso639();
        title = in.readString();
    }

    public String toString() {
        return "TitleBox[language=" + getLanguage() + ";title=" + getTitle() + "]";
    }
}
