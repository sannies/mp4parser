package org.mp4parser.boxes.iso23001.part7;

import org.mp4parser.tools.Hex;

import java.math.BigInteger;
import java.util.Arrays;

/**
 * Each encrypted sample in a protected track shall have an Initialization Vector associated with it. Further, each
 * encrypted sample in protected AVC video tracks shall conform to ISO/IEC 14496-10 and ISO/IEC 14496-15
 * and shall use the subsample encryption scheme specified in 9.6.2, which requires subsample encryption data.
 * Both initialization vectors and subsample encryption data are provided as Sample Auxiliary Information with
 * aux_info_type equal to ‘cenc’ and aux_info_type_parameter equal to 0. For tracks protected using
 * the 'cenc' scheme, the default value for aux_info_type is equal to 'cenc' and the default value for the
 * aux_info_type_parameter is 0 so content may be created omitting these optional fields. Storage of
 * sample auxiliary information shall conform to ISO/IEC 14496-12.<br>
 * This class can also be used for PIFF as it has been derived from the PIFF spec.
 */
public class CencSampleAuxiliaryDataFormat {
    public byte[] iv = new byte[0];
    public Pair[] pairs = null;

    public int getSize() {
        int size = iv.length;
        if (pairs != null && pairs.length > 0) {
            size += 2;
            size += (pairs.length * 6);
        }
        return size;
    }

    public Pair createPair(final int clear, long encrypted) {
        // Memory saving!!!
        if (clear <= Byte.MAX_VALUE) {
            if (encrypted <= Byte.MAX_VALUE) {
                return new ByteBytePair(clear, encrypted);
            } else if (encrypted <= Short.MAX_VALUE) {
                return new ByteShortPair(clear, encrypted);
            } else if (encrypted <= Integer.MAX_VALUE) {
                return new ByteIntPair(clear, encrypted);
            } else {
                return new ByteLongPair(clear, encrypted);
            }
        } else if (clear <= Short.MAX_VALUE) {
            if (encrypted <= Byte.MAX_VALUE) {
                return new ShortBytePair(clear, encrypted);
            } else if (encrypted <= Short.MAX_VALUE) {
                return new ShortShortPair(clear, encrypted);
            } else if (encrypted <= Integer.MAX_VALUE) {
                return new ShortIntPair(clear, encrypted);
            } else {
                return new ShortLongPair(clear, encrypted);
            }
        } else {
            if (encrypted <= Byte.MAX_VALUE) {
                return new IntBytePair(clear, encrypted);
            } else if (encrypted <= Short.MAX_VALUE) {
                return new IntShortPair(clear, encrypted);
            } else if (encrypted <= Integer.MAX_VALUE) {
                return new IntIntPair(clear, encrypted);
            } else {
                return new IntLongPair(clear, encrypted);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CencSampleAuxiliaryDataFormat entry = (CencSampleAuxiliaryDataFormat) o;

        if (!new BigInteger(iv).equals(new BigInteger(entry.iv))) {
            return false;
        }
        if (pairs != null ? !Arrays.equals(pairs, entry.pairs) : entry.pairs != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = iv != null ? Arrays.hashCode(iv) : 0;
        result = 31 * result + (pairs != null ? Arrays.hashCode(pairs) : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Entry{" +
                "iv=" + Hex.encodeHex(iv) +
                ", pairs=" + Arrays.toString(pairs) +
                '}';
    }

    public interface Pair {
        int clear();

        long encrypted();
    }

    private class ByteBytePair extends AbstractPair {
        private byte clear;
        private byte encrypted;

        public ByteBytePair(int clear, long encrypted) {
            this.clear = (byte) clear;
            this.encrypted = (byte) encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }

    }

    private class ByteShortPair extends AbstractPair {
        private byte clear;
        private short encrypted;

        public ByteShortPair(int clear, long encrypted) {
            this.clear = (byte) clear;
            this.encrypted = (short) encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }
    }

    private class ByteIntPair extends AbstractPair {
        private byte clear;
        private int encrypted;

        public ByteIntPair(int clear, long encrypted) {
            this.clear = (byte) clear;
            this.encrypted = (int) encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }
    }

    private class ByteLongPair extends AbstractPair {
        private byte clear;
        private long encrypted;

        public ByteLongPair(int clear, long encrypted) {
            this.clear = (byte) clear;
            this.encrypted = encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }
    }

    private class ShortBytePair extends AbstractPair {
        private short clear;
        private byte encrypted;

        public ShortBytePair(int clear, long encrypted) {
            this.clear = (short) clear;
            this.encrypted = (byte) encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }
    }

    private class ShortShortPair extends AbstractPair {
        private short clear;
        private short encrypted;

        public ShortShortPair(int clear, long encrypted) {
            this.clear = (short) clear;
            this.encrypted = (short) encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }
    }

    private class ShortIntPair extends AbstractPair {
        private short clear;
        private int encrypted;

        public ShortIntPair(int clear, long encrypted) {
            this.clear = (short) clear;
            this.encrypted = (int) encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }
    }

    private class ShortLongPair extends AbstractPair {
        private short clear;
        private long encrypted;

        public ShortLongPair(int clear, long encrypted) {
            this.clear = (short) clear;
            this.encrypted = encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }
    }

    private class IntBytePair extends AbstractPair {
        private int clear;
        private byte encrypted;

        public IntBytePair(int clear, long encrypted) {
            this.clear = clear;
            this.encrypted = (byte) encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }
    }

    private class IntShortPair extends AbstractPair {
        private int clear;
        private short encrypted;

        public IntShortPair(int clear, long encrypted) {
            this.clear = clear;
            this.encrypted = (short) encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }
    }

    private class IntIntPair extends AbstractPair {
        private int clear;
        private int encrypted;

        public IntIntPair(int clear, long encrypted) {
            this.clear = clear;
            this.encrypted = (int) encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }
    }

    private class IntLongPair extends AbstractPair {
        private int clear;
        private long encrypted;

        public IntLongPair(int clear, long encrypted) {
            this.clear = clear;
            this.encrypted = encrypted;
        }

        public int clear() {
            return clear;
        }

        public long encrypted() {
            return encrypted;
        }
    }

    private abstract class AbstractPair implements Pair {

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Pair pair = (Pair) o;

            if (clear() != pair.clear()) {
                return false;
            }
            if (encrypted() != pair.encrypted()) {
                return false;
            }

            return true;
        }

        public String toString() {
            return "P(" + clear() + "|" + encrypted() + ")";
        }
    }
}
