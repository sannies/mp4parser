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
 * The copyright box contains a copyright declaration which applies to the entire presentation, when contained
 * within the MovieBox, or, when contained in a track, to that entire track. There may be multple boxes using
 * different language codes.
 *
 * @see MovieBox
 * @see TrackBox
 */
public class CopyrightBox extends AbstractFullBox {
    public static final String TYPE = "cprt";

    private String language;
    private String copyright;

    public CopyrightBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getLanguage() {
        return language;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    protected long getContentSize() {
        try {
            return 2 + copyright.getBytes("UTF-8").length + 1;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        language = in.readIso639();
        copyright = in.readString();
    }

    public String toString() {
        return "CopyrightBox[language=" + getLanguage() + ";copyright=" + getCopyright() + "]";
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeIso639(language);
        os.writeStringZeroTerm(copyright);
    }


}
