/*
 * Copyright 2012 castLabs, Berlin
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

package com.googlecode.mp4parser.boxes.mp4.samplegrouping;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.util.UUIDConverter;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <p>Each sample in a protected track shall be associated with an IsEncrypted flag, IV_Size, and KID.
 * This can be accomplished by (a) relying on the default values in the TrackEncryptionBox
 * (see 8.2), or (b) specifying the parameters by sample group, or (c) using a combination of these two techniques.</p>
 * <p>When specifying the parameters by sample group, the SampleToGroupBox in the sample table or track
 * fragment specifies which samples use which sample group description from the SampleGroupDescriptionBox.</p>
 */
public class CencSampleEncryptionInformationGroupEntry extends GroupEntry {
    public static final String TYPE = "seig";

    private boolean isEncrypted;
    private byte ivSize;
    private UUID kid;

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        isEncrypted = IsoTypeReader.readUInt24(byteBuffer) == 1;
        ivSize = (byte) IsoTypeReader.readUInt8(byteBuffer);
        byte[] kid = new byte[16];
        byteBuffer.get(kid);
        this.kid = UUIDConverter.convert(kid);

    }

    @Override
    public ByteBuffer get() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);
        IsoTypeWriter.writeUInt24(byteBuffer, isEncrypted?1:0);
        if (isEncrypted) {
            IsoTypeWriter.writeUInt8(byteBuffer, ivSize);
            byteBuffer.put(UUIDConverter.convert(kid));
        } else {
            byteBuffer.put(new byte[17]);
        }
        byteBuffer.rewind();
        return byteBuffer;
    }

    public boolean isEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(boolean encrypted) {
        isEncrypted = encrypted;
    }

    public byte getIvSize() {
        return ivSize;
    }

    public void setIvSize(int ivSize) {
        this.ivSize = (byte) ivSize;
    }

    public UUID getKid() {
        return kid;
    }

    public void setKid(UUID kid) {
        this.kid = kid;
    }

    @Override
    public String toString() {
        return "CencSampleEncryptionInformationGroupEntry{" +
                "isEncrypted=" + isEncrypted +
                ", ivSize=" + ivSize +
                ", kid=" + kid +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CencSampleEncryptionInformationGroupEntry that = (CencSampleEncryptionInformationGroupEntry) o;

        if (isEncrypted != that.isEncrypted) return false;
        if (ivSize != that.ivSize) return false;
        if (kid != null ? !kid.equals(that.kid) : that.kid != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = isEncrypted?7:19;
        result = 31 * result + (int) ivSize;
        result = 31 * result + (kid != null ? kid.hashCode() : 0);
        return result;
    }
}
