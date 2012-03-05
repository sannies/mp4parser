package com.googlecode.mp4parser.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.AbstractFullBox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 *
 */
public abstract class AbstractTrackEncryptionBox extends AbstractFullBox {
    int defaultAlgorithmId;
    int defaultIvSize;
    byte[] default_KID;

    protected AbstractTrackEncryptionBox(String type) {
        super(type);
    }

    public int getDefaultAlgorithmId() {
        return defaultAlgorithmId;
    }

    public void setDefaultAlgorithmId(int defaultAlgorithmId) {
        this.defaultAlgorithmId = defaultAlgorithmId;
    }

    public int getDefaultIvSize() {
        return defaultIvSize;
    }

    public void setDefaultIvSize(int defaultIvSize) {
        this.defaultIvSize = defaultIvSize;
    }

    public byte[] getDefault_KID() {
        return default_KID;
    }

    public void setDefault_KID(byte[] default_KID) {
        this.default_KID = default_KID;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        defaultAlgorithmId = IsoTypeReader.readUInt24(content);
        defaultIvSize = IsoTypeReader.readUInt8(content);
        default_KID = new byte[16];
        content.get(default_KID);
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);
        IsoTypeWriter.writeUInt24(bb, defaultAlgorithmId);
        IsoTypeWriter.writeUInt8(bb, defaultIvSize);
        bb.put(default_KID);
    }

    @Override
    protected long getContentSize() {
        return 24;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractTrackEncryptionBox that = (AbstractTrackEncryptionBox) o;

        if (defaultAlgorithmId != that.defaultAlgorithmId) return false;
        if (defaultIvSize != that.defaultIvSize) return false;
        if (!Arrays.equals(default_KID, that.default_KID)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = defaultAlgorithmId;
        result = 31 * result + defaultIvSize;
        result = 31 * result + (default_KID != null ? Arrays.hashCode(default_KID) : 0);
        return result;
    }
}
