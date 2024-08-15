/*
 * Copyright 2009 castLabs GmbH, Berlin
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

package org.mp4parser.boxes.apple;

import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * See <a href="https://developer.apple.com/documentation/quicktime-file-format/timecode_media_information_atom">Apple Quicktime Format</a>
 */
public class TimeCodeMediaInformationBox extends AbstractFullBox {
    public static final String TYPE = "tcmi";

    private int textFont; //     A 16-bit integer that indicates the font to use.
    private int textFace; //     A 16-bit integer that indicates the font’s style.
    private int textSize; //    A 16-bit integer that specifies the point size of the time code text.
    private int reserved; //    A 16-bit integer that is reserved for use by Apple.
    private int textColorR; //     A 48-bit RGB color value for the timecode text - red.
    private int textColorG; //     A 48-bit RGB color value for the timecode text - green.
    private int textColorB; //     A 48-bit RGB color value for the timecode text - blue.
    private int backgroundColorR; //     A 48-bit RGB background color for the timecode text - red.
    private int backgroundColorG; //     A 48-bit RGB background color for the timecode text - green.
    private int backgroundColorB; //     A 48-bit RGB background color for the timecode text - blue.
    private String fontName; //    A Pascal string specifying the name of the timecode text’s font.

    public TimeCodeMediaInformationBox() {
        super(TYPE);
    }


    protected long getContentSize() {
        return 12 + 2 + 2 + 2 + 2 + 48 + 48 + 1 + fontName.length();
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        textFont = IsoTypeReader.readUInt16(content);
        textFace = IsoTypeReader.readUInt16(content);
        textSize = IsoTypeReader.readUInt16(content);
        reserved = IsoTypeReader.readUInt16(content);
        textColorR = IsoTypeReader.readUInt16(content);
        textColorG = IsoTypeReader.readUInt16(content);
        textColorB = IsoTypeReader.readUInt16(content);
        backgroundColorR = IsoTypeReader.readUInt16(content);
        backgroundColorG = IsoTypeReader.readUInt16(content);
        backgroundColorB = IsoTypeReader.readUInt16(content);
        int length = IsoTypeReader.readUInt8(content);
        byte[] fontNameBytes = new byte[length];
        content.get(fontNameBytes);
        fontName = new String(fontNameBytes, StandardCharsets.UTF_8);
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt16(byteBuffer, textFont);
        IsoTypeWriter.writeUInt16(byteBuffer, textFace);
        IsoTypeWriter.writeUInt16(byteBuffer, textSize);
        IsoTypeWriter.writeUInt16(byteBuffer, reserved);
        IsoTypeWriter.writeUInt16(byteBuffer, textColorR);
        IsoTypeWriter.writeUInt16(byteBuffer, textColorG);
        IsoTypeWriter.writeUInt16(byteBuffer, textColorB);
        IsoTypeWriter.writeUInt16(byteBuffer, backgroundColorR);
        IsoTypeWriter.writeUInt16(byteBuffer, backgroundColorG);
        IsoTypeWriter.writeUInt16(byteBuffer, backgroundColorB);
        IsoTypeWriter.writeUInt8(byteBuffer, fontName.length());
        byteBuffer.put(fontName.getBytes(StandardCharsets.UTF_8), 0, fontName.length());
    }

    public int getTextFont() {
        return textFont;
    }

    public void setTextFont(int textFont) {
        this.textFont = textFont;
    }

    public int getTextFace() {
        return textFace;
    }

    public void setTextFace(int textFace) {
        this.textFace = textFace;
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    public int getTextColorR() {
        return textColorR;
    }

    public void setTextColorR(int textColorR) {
        this.textColorR = textColorR;
    }

    public int getTextColorG() {
        return textColorG;
    }

    public void setTextColorG(int textColorG) {
        this.textColorG = textColorG;
    }

    public int getTextColorB() {
        return textColorB;
    }

    public void setTextColorB(int textColorB) {
        this.textColorB = textColorB;
    }

    public int getBackgroundColorR() {
        return backgroundColorR;
    }

    public void setBackgroundColorR(int backgroundColorR) {
        this.backgroundColorR = backgroundColorR;
    }

    public int getBackgroundColorG() {
        return backgroundColorG;
    }

    public void setBackgroundColorG(int backgroundColorG) {
        this.backgroundColorG = backgroundColorG;
    }

    public int getBackgroundColorB() {
        return backgroundColorB;
    }

    public void setBackgroundColorB(int backgroundColorB) {
        this.backgroundColorB = backgroundColorB;
    }

    public String getFontName() {
        return fontName;
    }

    public void setFontName(String fontName) {
        this.fontName = fontName;
    }
}
