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

package com.mp4parser.boxes.threegpp.ts26244;

import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.IsoTypeWriter;
import com.mp4parser.tools.Utf8;
import com.mp4parser.boxes.iso14496.part12.UserDataBox;
import com.mp4parser.support.AbstractFullBox;

import java.nio.ByteBuffer;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <pre>
 * Box Type: 'titl'
 * Container: {@link UserDataBox} ('udta')
 * Mandatory: No
 * Quantity: Zero or one
 * </pre>
 * Title for the media.
 */
public class TitleBox extends AbstractFullBox {
    public static final String TYPE = "titl";

    private String language;
    private String title;

    public TitleBox() {
        super(TYPE);
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

    protected long getContentSize() {
        return 7 + Utf8.utf8StringLengthInBytes(title);
    }


    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeIso639(byteBuffer, language);
        byteBuffer.put(Utf8.convert(title));
        byteBuffer.put((byte) 0);
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        language = IsoTypeReader.readIso639(content);
        title = IsoTypeReader.readString(content);
    }

    public String toString() {
        return "TitleBox[language=" + getLanguage() + ";title=" + getTitle() + "]";
    }
}
