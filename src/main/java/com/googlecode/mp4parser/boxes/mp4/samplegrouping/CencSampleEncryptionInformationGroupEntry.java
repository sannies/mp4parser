package com.googlecode.mp4parser.boxes.mp4.samplegrouping;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.authoring.builder.ByteBufferHelper;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Each sample in a protected track shall be associated with an IsEncrypted flag, IV_Size, and KID.
 * This can be accomplished by (a) relying on the default values in the TrackEncryptionBox
 * (see 8.2), or (b) specifying the parameters by sample group, or (c) using a combination of these two techniques.
 * <p/>
 * When specifying the parameters by sample group, the SampleToGroupBox in the sample table or track
 * fragment specifies which samples use which sample group description from the SampleGroupDescriptionBox.
 */
public class CencSampleEncryptionInformationGroupEntry extends GroupEntry {
    public static final String TYPE = "seig";

    int isEncrypted;
    byte ivSize;
    byte[] kid = new byte[16];

    @Override
    public void parse(ByteBuffer byteBuffer) {
        isEncrypted = IsoTypeReader.readUInt24(byteBuffer);
        ivSize = (byte) IsoTypeReader.readUInt8(byteBuffer);
        kid = new byte[16];
        byteBuffer.get(kid);

    }

    @Override
    public ByteBuffer get() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);
        IsoTypeWriter.writeUInt24(byteBuffer, isEncrypted);
        IsoTypeWriter.writeUInt8(byteBuffer, ivSize);
        byteBuffer.put(kid);
        byteBuffer.rewind();
        return byteBuffer;
    }

    public int getEncrypted() {
        return isEncrypted;
    }

    public void setEncrypted(int encrypted) {
        isEncrypted = encrypted;
    }

    public byte getIvSize() {
        return ivSize;
    }

    public void setIvSize(byte ivSize) {
        this.ivSize = ivSize;
    }

    public byte[] getKid() {
        return kid;
    }

    public void setKid(byte[] kid) {
        assert kid.length == 16;
        this.kid = kid;
    }

    @Override
    public String toString() {
        return "CencSampleEncryptionInformationGroupEntry{" +
                "isEncrypted=" + isEncrypted +
                ", ivSize=" + ivSize +
                ", kid=" + Hex.encodeHex(kid) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CencSampleEncryptionInformationGroupEntry that = (CencSampleEncryptionInformationGroupEntry) o;

        if (isEncrypted != that.isEncrypted) {
            return false;
        }
        if (ivSize != that.ivSize) {
            return false;
        }
        if (!Arrays.equals(kid, that.kid)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = isEncrypted;
        result = 31 * result + (int) ivSize;
        result = 31 * result + (kid != null ? Arrays.hashCode(kid) : 0);
        return result;
    }
}
