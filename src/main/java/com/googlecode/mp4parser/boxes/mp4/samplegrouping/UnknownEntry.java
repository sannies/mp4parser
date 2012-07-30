package com.googlecode.mp4parser.boxes.mp4.samplegrouping;

import com.coremedia.iso.Hex;

import java.nio.ByteBuffer;

/**
 *
 */
public class UnknownEntry extends GroupEntry {
    ByteBuffer content;

    public UnknownEntry() {
    }

    public ByteBuffer getContent() {
        return content;
    }

    public void setContent(ByteBuffer content) {
        this.content = (ByteBuffer) content.duplicate().rewind();
    }

    @Override
    public void parse(ByteBuffer byteBuffer) {
        this.content = (ByteBuffer) byteBuffer.duplicate().rewind();
    }

    @Override
    public ByteBuffer get() {
        return content.duplicate();
    }

    @Override
    public String toString() {
        ByteBuffer bb = content.duplicate();
        bb.rewind();
        byte[] b = new byte[bb.limit()];
        bb.get(b);
        return "UnknownEntry{" +
                "content=" + Hex.encodeHex(b) +
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

        UnknownEntry that = (UnknownEntry) o;

        if (content != null ? !content.equals(that.content) : that.content != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return content != null ? content.hashCode() : 0;
    }
}
