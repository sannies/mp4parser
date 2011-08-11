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
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * The GroupId value identifies this DCF as part of a group of DCFs whose Rights can be defined
 * in a common group Rights Object instead of (or in addition to) in separate content-specific
 * Rights Objects. Located in extended headers in {@link OmaDrmCommonHeadersBox}.
 */
public class OmaDrmGroupIdBox extends AbstractFullBox {
    public static final String TYPE = "grpi";

    private int gkEncryptionMethod;
    private String groupId;
    private byte[] groupKey;

    public OmaDrmGroupIdBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public void setGkEncryptionMethod(int gkEncryptionMethod) {
        this.gkEncryptionMethod = gkEncryptionMethod;
    }

    public int getGkEncryptionMethod() {
        return gkEncryptionMethod;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupKey(byte[] groupKey) {
        this.groupKey = groupKey;
    }

    public byte[] getGroupKey() {
        return groupKey;
    }

    protected long getContentSize() {
        return 5 + utf8StringLengthInBytes(groupId) + groupKey.length;
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        int groupIdLength = in.readUInt16();
        gkEncryptionMethod = in.readUInt8();
        int gkLength = in.readUInt16();
        groupId = new String(in.read(groupIdLength), "UTF-8");
        groupKey = in.read(gkLength);
    }

    protected void getContent(IsoOutputStream isos) throws IOException {
        isos.writeUInt16(utf8StringLengthInBytes(groupId));
        isos.writeUInt8(gkEncryptionMethod);
        isos.writeUInt16(groupKey.length);
        isos.writeStringNoTerm(groupId);
        isos.write(groupKey);
    }

    public String toString() {
        return "OmaDrmGroupIdBox[gkEncryptionMethod=" + getGkEncryptionMethod() + ";groupId=" + getGroupId() + "]";
    }
}
