package org.mp4parser.muxer.samples;

import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.SampleImpl;
import org.mp4parser.tools.RangeStartMap;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractList;
import java.util.List;

import static org.mp4parser.tools.CastUtils.l2i;


public class CencDecryptingSampleList extends AbstractList<Sample> {

    List<CencSampleAuxiliaryDataFormat> sencInfo;
    RangeStartMap<Integer, SecretKey> keys = new RangeStartMap<Integer, SecretKey>();
    List<Sample> parent;
    String encryptionAlgo;

    public CencDecryptingSampleList(SecretKey secretKey, List<Sample> parent, List<CencSampleAuxiliaryDataFormat> sencInfo) {
        this(new RangeStartMap<Integer, SecretKey>(0, secretKey), parent, sencInfo, "cenc");

    }

    public CencDecryptingSampleList(
            RangeStartMap<Integer, SecretKey> keys,
            List<Sample> parent, List<CencSampleAuxiliaryDataFormat> sencInfo,
            String encryptionAlgo) {
        this.sencInfo = sencInfo;
        this.keys = keys;
        this.parent = parent;
        this.encryptionAlgo = encryptionAlgo;
    }


    Cipher getCipher(SecretKey sk, byte[] iv) {
        byte[] fullIv = new byte[16];
        System.arraycopy(iv, 0, fullIv, 0, iv.length);
        // The IV
        try {
            if ("cenc".equals(encryptionAlgo)) {
                Cipher c = Cipher.getInstance("AES/CTR/NoPadding");
                c.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(fullIv));
                return c;
            } else if ("cbc1".equals(encryptionAlgo)) {
                Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
                c.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(fullIv));
                return c;
            } else {
                throw new RuntimeException("Only cenc & cbc1 is supported as encryptionAlgo");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }


    }

    @Override
    public Sample get(int index) {
        if (keys.get(index) != null) {
            Sample encSample = parent.get(index);
            final ByteBuffer encSampleBuffer = encSample.asByteBuffer();
            encSampleBuffer.rewind();
            final ByteBuffer decSampleBuffer = ByteBuffer.allocate(encSampleBuffer.limit());
            final CencSampleAuxiliaryDataFormat sencEntry = sencInfo.get(index);
            Cipher cipher = getCipher(keys.get(index), sencEntry.iv);
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
                        System.err.println("Decrypted sample but still data remaining: " + encSample.getSize());
                    }
                    decSampleBuffer.put(cipher.doFinal());
                } else {
                    byte[] fullyEncryptedSample = new byte[encSampleBuffer.limit()];
                    encSampleBuffer.get(fullyEncryptedSample);
                    if ("cbc1".equals(encryptionAlgo)) {
                        int encryptedLength = fullyEncryptedSample.length / 16 * 16;
                        decSampleBuffer.put(cipher.doFinal(fullyEncryptedSample, 0, encryptedLength));
                        decSampleBuffer.put(fullyEncryptedSample, encryptedLength, fullyEncryptedSample.length - encryptedLength);
                    } else if ("cenc".equals(encryptionAlgo)) {
                        decSampleBuffer.put(cipher.doFinal(fullyEncryptedSample));
                    }
                }
                encSampleBuffer.rewind();
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            }
            decSampleBuffer.rewind();
            return new SampleImpl(decSampleBuffer);
        } else {
            return parent.get(index);
        }
    }

    @Override
    public int size() {
        return parent.size();
    }
}
