package com.googlecode.mp4parser.boxes;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.AbstractFullBox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;


public abstract class AbstractSampleEncryptionBox extends AbstractFullBox {
    int algorithmId = -1;
    int ivSize = -1;
    byte[] kid = new byte[16];
    List<Entry> entries = new LinkedList<Entry>();

    protected AbstractSampleEncryptionBox(String type) {
        super(type);
    }

    public int getOffsetToFirstIV() {
        int offset = (getSize() > (1l << 32) ? 16 : 8);
        offset += isOverrideTrackEncryptionBoxParameters() ? 20 : 0;
        offset += 4; //num entries
        return offset;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        if ((getFlags() & 0x1) > 0) {
            algorithmId = IsoTypeReader.readUInt24(content);
            ivSize = IsoTypeReader.readUInt8(content);
            kid = new byte[16];
            content.get(kid);
        }
        long numOfEntries = IsoTypeReader.readUInt32(content);
        while (numOfEntries-- > 0) {
            Entry e = new Entry();
            e.iv = new byte[((getFlags() & 0x1) > 0) ? ivSize : 8];
            content.get(e.iv);
            if ((getFlags() & 0x2) > 0) {
                int numOfPairs = IsoTypeReader.readUInt16(content);
                while (numOfPairs-- > 0) {
                    e.pairs.add(new Entry.Pair(IsoTypeReader.readUInt16(content), IsoTypeReader.readUInt32(content)));
                }
            }
            entries.add(e);

        }
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);
        if (isOverrideTrackEncryptionBoxParameters()) {
            IsoTypeWriter.writeUInt24(bb, algorithmId);
            IsoTypeWriter.writeUInt8(bb, ivSize);
            bb.put(kid);
        }
        IsoTypeWriter.writeUInt32(bb, entries.size());
        for (Entry entry : entries) {
            bb.put(entry.iv);
            if (isSubSampleEncryption()) {
                IsoTypeWriter.writeUInt16(bb, entry.pairs.size());
                for (Entry.Pair pair : entry.pairs) {
                    IsoTypeWriter.writeUInt16(bb, pair.clear);
                    IsoTypeWriter.writeUInt32(bb, pair.encrypted);
                }
            }
        }
    }


    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public int getAlgorithmId() {
        return algorithmId;
    }

    public void setAlgorithmId(int algorithmId) {
        this.algorithmId = algorithmId;
    }

    public int getIvSize() {
        return ivSize;
    }

    public void setIvSize(int ivSize) {
        this.ivSize = ivSize;
    }

    public byte[] getKid() {
        return kid;
    }

    public void setKid(byte[] kid) {
        this.kid = kid;
    }


    public boolean isSubSampleEncryption() {
        return (entries.get(0).pairs.size() > 0);
    }

    public boolean isOverrideTrackEncryptionBoxParameters() {
        return kid != null && algorithmId > 0 && ivSize > 0;
    }

    @Override
    protected long getContentSize() {
        long contentSize = 4;
        if (isOverrideTrackEncryptionBoxParameters()) {
            contentSize += 4;
            contentSize += kid.length;
        }
        contentSize += 4;
        for (Entry entry : entries) {
            contentSize += entry.iv.length;

            if (isSubSampleEncryption()) {
                contentSize += 2;
                for (Entry.Pair pair : entry.pairs) {
                    contentSize += 6;
                }
            }
        }
        return contentSize;
    }

    @Override
    public void getBox(WritableByteChannel os) throws IOException {
        setFlags(0x0);
        if (isOverrideTrackEncryptionBoxParameters()) {
            setFlags(getFlags() | 0x1);
        }
        if (isSubSampleEncryption()) {
            setFlags(getFlags() | 0x2);
        }

        super.getBox(os);
    }

    public static class Entry {
        public byte[] iv;
        public List<Pair> pairs = new LinkedList<Pair>();

        public static class Pair {
            public int clear;
            public long encrypted;

            public Pair(int clear, long encrypted) {
                this.clear = clear;
                this.encrypted = encrypted;
            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;

                Pair pair = (Pair) o;

                if (clear != pair.clear) return false;
                if (encrypted != pair.encrypted) return false;

                return true;
            }

            @Override
            public int hashCode() {
                int result = clear;
                result = 31 * result + (int) (encrypted ^ (encrypted >>> 32));
                return result;
            }

            @Override
            public String toString() {
                return "clr:" + clear + " enc:" + encrypted;
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (!Arrays.equals(iv, entry.iv)) return false;
            if (pairs != null ? !pairs.equals(entry.pairs) : entry.pairs != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = iv != null ? Arrays.hashCode(iv) : 0;
            result = 31 * result + (pairs != null ? pairs.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "iv=" + Hex.encodeHex(iv) +
                    ", pairs=" + pairs +
                    '}';
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractSampleEncryptionBox that = (AbstractSampleEncryptionBox) o;

        if (algorithmId != that.algorithmId) return false;
        if (ivSize != that.ivSize) return false;
        if (entries != null ? !entries.equals(that.entries) : that.entries != null) return false;
        if (!Arrays.equals(kid, that.kid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = algorithmId;
        result = 31 * result + ivSize;
        result = 31 * result + (kid != null ? Arrays.hashCode(kid) : 0);
        result = 31 * result + (entries != null ? entries.hashCode() : 0);
        return result;
    }

    public List<Short> getEntrySizes() {
        List<Short> entrySizes = new ArrayList<Short>(entries.size());
        for (Entry entry : entries) {
            short size = (short) entry.iv.length;
            if (isSubSampleEncryption()) {
                size += 2; //numPairs
                size += entry.pairs.size() * 6;
            }
            entrySizes.add(size);
        }
        return entrySizes;
    }
}
