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

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.Utf8;
import com.coremedia.iso.boxes.AbstractFullBox;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * A box in the {@link com.coremedia.iso.boxes.UserDataBox} containing information about the lyric location.
 * Invented by Vodafone.
 */
public class LyricsUriBox extends AbstractFullBox {
    public static final String TYPE = "lrcu";

    private String lyricsUri;

    public LyricsUriBox() {
        super(TYPE);
    }

    public String getLyricsUri() {
        return lyricsUri;
    }

    public void setLyricsUri(String lyricsUri) {
        this.lyricsUri = lyricsUri;
    }

    protected long getContentSize() {
        return Utf8.utf8StringLengthInBytes(lyricsUri) + 5;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        lyricsUri = IsoTypeReader.readString(content);
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);
        bb.put(Utf8.convert(lyricsUri));
        bb.put((byte) 0);
    }

    public String toString() {
        return "LyricsUriBox[lyricsUri=" + getLyricsUri() + "]";
    }
}
