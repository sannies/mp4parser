package com.googlecode.mp4parser.boxes.cenc;

import com.coremedia.iso.Hex;
import com.googlecode.mp4parser.boxes.AbstractSampleEncryptionBox;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Each encrypted sample in a protected track shall have an Initialization Vector associated with it. Further, each
 * encrypted sample in protected AVC video tracks shall conform to ISO/IEC 14496-10 and ISO/IEC 14496-15
 * and shall use the subsample encryption scheme specified in 9.6.2, which requires subsample encryption data.
 * Both initialization vectors and subsample encryption data are provided as Sample Auxiliary Information with
 * aux_info_typeequal to ‘cenc’ and aux_info_type_parameterequal to 0. For tracks protected using
 * the 'cenc' scheme, the default value for aux_info_type is equal to 'cenc' and the default value for the
 * aux_info_type_parameter is 0 so content may be created omitting these optional fields. Storage of
 * sample auxiliary information shall conform to ISO/IEC 14496-12.
 *
 * This class can also be used for PIFF as it has been derived from the PIFF spec.
 */
public class CencSampleAuxiliaryDataFormat {
    public byte[] iv;
    public List<Pair> pairs = new LinkedList<Pair>();

    public int getSize() {
        int size = iv.length;
        if (pairs != null && pairs.size() > 0) {
            size += 2;
            size += (pairs.size() * 6);
        }
        return size;
    }

    public Pair createPair(int clear, long encrypted) {
        return new Pair(clear, encrypted);
    }


    public class Pair {
        public int clear;
        public long encrypted;

        public Pair(int clear, long encrypted) {
            this.clear = clear;
            this.encrypted = encrypted;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            Pair pair = (Pair) o;

            if (clear != pair.clear) {
                return false;
            }
            if (encrypted != pair.encrypted) {
                return false;
            }

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
        if (pairs != null ? !pairs.equals(entry.pairs) : entry.pairs != null) {
            return false;
        }

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
