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
 * Used to give information about the performer. Mostly used in confunction with music files.
 * See 3GPP 26.234 for details.
 */
public class PerformerBox extends AbstractFullBox {
    public static final String TYPE = "perf";

    private String language;
    private String performer;

    public PerformerBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getLanguage() {
        return language;
    }

    public String getPerformer() {
        return performer;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setPerformer(String performer) {
        this.performer = performer;
    }

    protected long getContentSize() {
        return 2 + utf8StringLengthInBytes(performer) + 1;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        language = in.readIso639();
        performer = in.readString();
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeIso639(language);
        isos.writeStringZeroTerm(performer);
    }

    public String toString() {
        return "PerformerBox[language=" + getLanguage() + ";performer=" + getPerformer() + "]";
    }
}
