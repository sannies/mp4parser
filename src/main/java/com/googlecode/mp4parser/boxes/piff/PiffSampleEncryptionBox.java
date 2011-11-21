package com.googlecode.mp4parser.boxes.piff;

import com.coremedia.iso.*;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * aligned(8) class SampleEncryptionBox extends FullBox(‘uuid’, extended_type= 0xA2394F52-5A9B-4f14-A244-6C427C648DF4, version=0, flags=0)
 * {
 * <p/>
 * unsigned int (32) sample_count;
 * {
 * unsigned int(16) InitializationVector;
 * }[ sample_count ]
 * }
 */
public class PiffSampleEncryptionBox extends AbstractFullBox {
    int algorithmId = -1;
    int ivSize = -1;
    byte[] kid = null;
    List<Entry> entries = new LinkedList<Entry>();

    /**
     * Creates a SampleEncryptionBox for non-h264 tracks.
     */
    public PiffSampleEncryptionBox() {
        super(IsoFile.fourCCtoBytes("uuid"));

    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    @Override
    public byte[] getUserType() {
        return new byte[]{(byte) 0xA2, 0x39, 0x4F, 0x52, 0x5A, (byte) 0x9B, 0x4f, 0x14, (byte) 0xA2, 0x44, 0x6C, 0x42, 0x7C, 0x64, (byte) 0x8D, (byte) 0xF4};
    }


    @Override
    protected long getContentSize() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            getContent(new IsoOutputStream(baos));
        } catch (IOException e) {
            return -1;
        }
        return baos.toByteArray().length;
    }

    public boolean isSubSampleEncryption() {
        return (entries.get(0).pairs.size() > 0);
    }

    public boolean isOverrideTrackEncryptionBoxParameters() {
        return kid != null && algorithmId > 0 && ivSize > 0;
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

    @Override
    public void getBox(IsoOutputStream os) throws IOException {
        setFlags(0x0);
        if (isOverrideTrackEncryptionBoxParameters()) {
            setFlags(getFlags() | 0x1);
        }
        if (isSubSampleEncryption()) {
            setFlags(getFlags() | 0x2);
        }

        super.getBox(os);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        if ((getFlags() & 0x1) > 0) {
            algorithmId = in.readUInt24();
            ivSize = in.readUInt8();
            kid = in.read(16);
        }
        long numOfEntries = in.readUInt32();
        while (numOfEntries-- > 0) {
            Entry e = new Entry();
            e.iv = in.read(((getFlags() & 0x1) > 0) ? ivSize : 8);
            if ((getFlags() & 0x2) > 0) {
                int numOfPairs = in.readUInt16();
                while (numOfPairs-- > 0) {
                    e.pairs.add(new Entry.Pair(in.readUInt16(), in.readUInt32()));
                }
            }
            entries.add(e);

        }
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        if (isOverrideTrackEncryptionBoxParameters()) {
            os.writeUInt24(algorithmId);
            os.writeUInt8(ivSize);
            os.write(kid);
        }
        os.writeUInt32(entries.size());
        for (Entry entry : entries) {
            os.write(entry.iv);
            if (isSubSampleEncryption()) {
                os.writeUInt16(entry.pairs.size());
                for (Entry.Pair pair : entry.pairs) {
                    os.writeUInt16(pair.clear);
                    os.writeUInt32(pair.encrypted);
                }
            }
        }
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

        PiffSampleEncryptionBox that = (PiffSampleEncryptionBox) o;

        if (algorithmId != that.algorithmId) return false;
        if (ivSize != that.ivSize) return false;
        if (!entries.equals(that.entries)) return false;
        if (!Arrays.equals(kid, that.kid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = algorithmId;
        result = 31 * result + ivSize;
        result = 31 * result + (kid != null ? Arrays.hashCode(kid) : 0);
        result = 31 * result + entries.hashCode();
        return result;
    }
}
