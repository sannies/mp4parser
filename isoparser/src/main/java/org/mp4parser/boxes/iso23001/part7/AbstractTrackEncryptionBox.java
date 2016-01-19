package org.mp4parser.boxes.iso23001.part7;

import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.UUID;

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

    public UUID getDefault_KID() {
        ByteBuffer b = ByteBuffer.wrap(default_KID);
        b.order(ByteOrder.BIG_ENDIAN);
        return new UUID(b.getLong(), b.getLong());
    }

    public void setDefault_KID(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        this.default_KID = bb.array();
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
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt24(byteBuffer, defaultAlgorithmId);
        IsoTypeWriter.writeUInt8(byteBuffer, defaultIvSize);
        byteBuffer.put(default_KID);
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
