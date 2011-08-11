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
 * Classification of the media according to 3GPP 26.244.
 */
public class ClassificationBox extends AbstractFullBox {
    public static final String TYPE = "clsf";


    private String classificationEntity;
    private int classificationTableIndex;
    private String language;
    private String classificationInfo;

    public ClassificationBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getLanguage() {
        return language;
    }

    public String getClassificationEntity() {
        return classificationEntity;
    }

    public int getClassificationTableIndex() {
        return classificationTableIndex;
    }

    public String getClassificationInfo() {
        return classificationInfo;
    }

    public void setClassificationEntity(String classificationEntity) {
        this.classificationEntity = classificationEntity;
    }

    public void setClassificationTableIndex(int classificationTableIndex) {
        this.classificationTableIndex = classificationTableIndex;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setClassificationInfo(String classificationInfo) {
        this.classificationInfo = classificationInfo;
    }

    protected long getContentSize() {
        return 4 + 2 + 2 + utf8StringLengthInBytes(classificationInfo) + 1;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        classificationEntity = IsoFile.bytesToFourCC(in.read(4));
        classificationTableIndex = in.readUInt16();
        language = in.readIso639();
        classificationInfo = in.readString();
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.write(IsoFile.fourCCtoBytes(classificationEntity));
        isos.writeUInt16(classificationTableIndex);
        isos.writeIso639(language);
        isos.writeStringZeroTerm(classificationInfo);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("ClassificationBox[language=").append(getLanguage());
        buffer.append("classificationEntity=").append(getClassificationEntity());
        buffer.append(";classificationTableIndex=").append(getClassificationTableIndex());
        buffer.append(";language=").append(getLanguage());
        buffer.append(";classificationInfo=").append(getClassificationInfo());
        buffer.append("]");
        return buffer.toString();
    }
}
