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

package com.coremedia.iso.boxes.odf;


import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxInterface;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.FullBox;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The Common Headers Box defines a structure for required headers in a DCF file.
 * See OMA-TS-DRM-DCF-V2_0-*  specification for details.
 */
public class OmaDrmCommonHeadersBox extends FullBox implements ContainerBox {
    public static final String TYPE = "ohdr";

    private Box[] extendedHeaders;
    private int encryptionMethod;
    private int paddingScheme;
    private long plaintextLength;
    private String contentId;
    private String rightsIssuerUrl;
    private String textualHeaders;

    @SuppressWarnings("unchecked")
    public <T extends BoxInterface> T[] getBoxes(Class<T> clazz) {
        ArrayList<T> boxesToBeReturned = new ArrayList<T>();
        for (Box boxe : extendedHeaders) {
            if (clazz.isInstance(boxe)) {
                boxesToBeReturned.add(clazz.cast(boxe));
            }
        }
        return boxesToBeReturned.toArray((T[]) Array.newInstance(clazz, boxesToBeReturned.size()));
    }

    public OmaDrmCommonHeadersBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
        contentId = "";
        rightsIssuerUrl = "";
        textualHeaders = "";
        extendedHeaders = new Box[0];
    }

    public BoxInterface[] getBoxes() {
        return extendedHeaders;
    }

    public void setTextualHeaders(Map<String, String> m) {
        textualHeaders = "";
        if (m != null) {
            for (String key : m.keySet()) {
                String value = m.get(key);
                textualHeaders += key + ":";
                textualHeaders += value + "\0";
            }
        }
    }

    public void setRightsIssuerUrl(String rightsIssuerUrl) {
        assert rightsIssuerUrl != null;
        this.rightsIssuerUrl = rightsIssuerUrl;
    }

    public void setContentId(String contentId) {
        assert contentId != null;
        this.contentId = contentId;
    }

    public void setPlaintextLength(long plaintextLength) {
        this.plaintextLength = plaintextLength;
    }

    public void setPaddingScheme(int paddingScheme) {
        assert paddingScheme == 0 || paddingScheme == 1;
        this.paddingScheme = paddingScheme;
    }

    public void setEncryptionMethod(int encryptionMethod) {
        assert encryptionMethod == 0 || encryptionMethod == 1 || encryptionMethod == 2;
        this.encryptionMethod = encryptionMethod;
    }

    public int getEncryptionMethod() {
        return encryptionMethod;
    }

    public int getPaddingScheme() {
        return paddingScheme;
    }

    public long getPlaintextLength() {
        return plaintextLength;
    }

    public String getContentId() {
        return contentId;
    }

    public String getRightsIssuerUrl() {
        return rightsIssuerUrl;
    }

    public String getTextualHeaders() {
        return textualHeaders;
    }

    public String getDisplayName() {
        return "OMA DRM Common Headers Box";
    }

    protected long getContentSize() {
        long contentLength;
        try {
            contentLength = 16 +
                    contentId.getBytes("UTF-8").length + (rightsIssuerUrl != null ? rightsIssuerUrl.getBytes("UTF-8").length : 0) +
                    (textualHeaders != null ? textualHeaders.getBytes("UTF-8").length : 0);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        for (Box boxe : extendedHeaders) {
            contentLength += boxe.getSize();
        }

        return contentLength;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        encryptionMethod = in.readUInt8();
        paddingScheme = in.readUInt8();
        plaintextLength = in.readUInt64();
        int contentIdLength = in.readUInt16();
        int rightsIssuerUrlLength = in.readUInt16();
        int textualHeadersLength = in.readUInt16();
        contentId = new String(in.read(contentIdLength), "UTF-8");
        rightsIssuerUrl = new String(in.read(rightsIssuerUrlLength), "UTF-8");
        textualHeaders = new String(in.read(textualHeadersLength), "UTF-8");
        List<BoxInterface> boxeList = new LinkedList<BoxInterface>();
        long remainingContentSize = size;
        remainingContentSize -= 4 + 1 + 1 + 8 + 2 + 2 + 2;
        remainingContentSize -= contentIdLength + rightsIssuerUrlLength + textualHeadersLength;
        while (remainingContentSize > 0) {
            BoxInterface box = boxParser.parseBox(in, this, lastMovieFragmentBox);
            remainingContentSize -= box.getSize();
            boxeList.add(box);
        }
        assert remainingContentSize == 0;
        this.extendedHeaders = boxeList.toArray(new Box[boxeList.size()]);
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeUInt8(encryptionMethod);
        isos.writeUInt8(paddingScheme);
        isos.writeUInt64(plaintextLength);
        isos.writeUInt16(utf8StringLengthInBytes(contentId));
        isos.writeUInt16(rightsIssuerUrl != null ? utf8StringLengthInBytes(rightsIssuerUrl) : 0);
        isos.writeUInt16(textualHeaders != null ? utf8StringLengthInBytes(textualHeaders) : 0);
        isos.writeStringNoTerm(contentId);
        isos.writeStringNoTerm(rightsIssuerUrl);
        isos.writeStringNoTerm(textualHeaders);

        for (Box boxe : extendedHeaders) {
            boxe.getBox(isos);
        }

    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("OmaDrmCommonHeadersBox[");
        buffer.append("encryptionMethod=").append(getEncryptionMethod()).append(";");
        buffer.append("paddingScheme=").append(getPaddingScheme()).append(";");
        buffer.append("plaintextLength=").append(getPlaintextLength()).append(";");
        buffer.append("contentId=").append(getContentId()).append(";");
        buffer.append("rightsIssuerUrl=").append(getRightsIssuerUrl()).append(";");
        buffer.append("textualHeaders=").append(getTextualHeaders());
        for (BoxInterface box : getBoxes()) {
            buffer.append(";");
            buffer.append(box.toString());
        }
        buffer.append("]");
        return buffer.toString();
    }

    public void setExtendedHeaders(Box[] extendedHeaders) {
        this.extendedHeaders = extendedHeaders;
    }

    public long getNumOfBytesToFirstChild() {
        long sizeOfChildren = 0;
        for (Box extendedHeader : extendedHeaders) {
            sizeOfChildren += extendedHeader.getSize();
        }
        return getSize() - sizeOfChildren;
    }
}
