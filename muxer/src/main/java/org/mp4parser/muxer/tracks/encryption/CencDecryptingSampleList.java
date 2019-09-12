package org.mp4parser.muxer.tracks.encryption;

import org.mp4parser.Container;
import org.mp4parser.boxes.iso14496.part12.SchemeTypeBox;
import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.SampleImpl;
import org.mp4parser.tools.Hex;
import org.mp4parser.tools.Path;
import org.mp4parser.tools.RangeStartMap;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;

import static org.mp4parser.tools.CastUtils.l2i;


public class CencDecryptingSampleList extends AbstractList<Sample> {

    private RangeStartMap<Integer, SampleEntry> sampleEntries;
    private List<CencSampleAuxiliaryDataFormat> sencInfo;
    private RangeStartMap<Integer, SecretKey> keys = new RangeStartMap<>();
    private List<Sample> parent;

    public CencDecryptingSampleList(
            RangeStartMap<Integer, SecretKey> keys,
            RangeStartMap<Integer, SampleEntry> sampleEntries,
            List<Sample> parent,
            List<CencSampleAuxiliaryDataFormat> sencInfo
    ) {
        this.sampleEntries = sampleEntries;
        this.sencInfo = sencInfo;
        this.keys = keys;
        this.parent = parent;
    }

    private String getSchemeType(SampleEntry s) {
        SchemeTypeBox schm = Path.getPath((Container) s, "sinf/schm");

         assert schm != null : "Cannot get cipher without schemetypebox";
        return schm.getSchemeType();
    }

    private Cipher getCipher(SecretKey sk, byte[] iv, SampleEntry se) {

        byte[] fullIv = new byte[16];
        System.arraycopy(iv, 0, fullIv, 0, iv.length);
        // The IV
        try {
            String schemeType = getSchemeType(se);
            if ("cenc".equals(schemeType) || "piff".equals(schemeType)) {
                Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
                c.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(fullIv));
                return c;
            } else if ("cbc1".equals(schemeType)) {
                Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
                c.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(fullIv));
                return c;
            } else {
                throw new RuntimeException("Only cenc & cbc1 is supported as encryptionAlgo");
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public Sample get(int index) {
        if (keys.get(index) != null) {
            Sample encSample = parent.get(index);
            final ByteBuffer encSampleBuffer = encSample.asByteBuffer();
            ((Buffer)encSampleBuffer).rewind();
            final ByteBuffer decSampleBuffer = ByteBuffer.allocate(encSampleBuffer.limit());
            final CencSampleAuxiliaryDataFormat sencEntry = sencInfo.get(index);
            Cipher cipher = getCipher(keys.get(index), sencEntry.iv, encSample.getSampleEntry());

            try {
                if (sencEntry.pairs != null && sencEntry.pairs.length > 0) {

                    for (CencSampleAuxiliaryDataFormat.Pair pair : sencEntry.pairs) {
                        final int clearBytes = pair.clear();
                        final int encrypted = l2i(pair.encrypted());

                        byte[] clears = new byte[clearBytes];
                        encSampleBuffer.get(clears);
                        decSampleBuffer.put(clears);
                        if (encrypted > 0) {
                            byte[] encs = new byte[encrypted];
                            encSampleBuffer.get(encs);
                            final byte[] decr = cipher.update(encs);
                            decSampleBuffer.put(decr);
                        }

                    }
                    if (encSampleBuffer.remaining() > 0) {
                        System.err.println("Decrypted sample " + index + " but still data remaining: " + encSample.getSize());
                    }
                    decSampleBuffer.put(cipher.doFinal());
                } else {
                    byte[] fullyEncryptedSample = new byte[encSampleBuffer.limit()];
                    encSampleBuffer.get(fullyEncryptedSample);
                    String schemeType = getSchemeType(encSample.getSampleEntry());
                    if ("cbc1".equals(schemeType)) {
                        int encryptedLength = fullyEncryptedSample.length / 16 * 16;
                        decSampleBuffer.put(cipher.doFinal(fullyEncryptedSample, 0, encryptedLength));
                        decSampleBuffer.put(fullyEncryptedSample, encryptedLength, fullyEncryptedSample.length - encryptedLength);
                    } else if ("cenc".equals(schemeType)) {
                        decSampleBuffer.put(cipher.doFinal(fullyEncryptedSample));
                    } else if ("piff".equals(schemeType)) {
                        decSampleBuffer.put(cipher.doFinal(fullyEncryptedSample));
                    } else {
                        throw new RuntimeException("unknown encryption algo");
                    }
                }
                ((Buffer)encSampleBuffer).rewind();
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
            ((Buffer)decSampleBuffer).rewind();
            return new SampleImpl(decSampleBuffer, sampleEntries.get(index));
        } else {
            return parent.get(index);
        }
    }

    @Override
    public int size() {
        return parent.size();
    }
}
