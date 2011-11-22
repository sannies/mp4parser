package com.googlecode.mp4parser.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;import java.lang.Override;
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
    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt24(defaultAlgorithmId);
        os.writeUInt8(defaultIvSize);
        os.write(default_KID);
    }

    @Override
    protected long getContentSize() {
        return 20;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        defaultAlgorithmId = in.readUInt24();
        defaultIvSize = in.readUInt8();
        default_KID = in.read(16);
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
