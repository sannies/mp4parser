/*
 * Copyright © 2012, castLabs GmbH, www.castlabs.com
 */

package com.googlecode.mp4parser.boxes.cenc;


import com.googlecode.mp4parser.authoring.Sample;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractList;
import java.util.List;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

public class CommonEncryptionSampleList extends AbstractList<Sample> {

    List<CencSampleAuxiliaryDataFormat> auxiliaryDataFormats;
    SecretKey secretKey;
    List<Sample> parent;
    static Cipher cipher;

    static {
        try {
            cipher = Cipher.getInstance("AES/CTR/NoPadding");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    public CommonEncryptionSampleList(
            SecretKey secretKey,
            List<Sample> parent,
            List<CencSampleAuxiliaryDataFormat> auxiliaryDataFormats) {
        this.auxiliaryDataFormats = auxiliaryDataFormats;
        this.secretKey = secretKey;
        this.parent = parent;
    }

    @Override
    public Sample get(int index) {
        Sample clearSample = parent.get(index);
        CencSampleAuxiliaryDataFormat entry = auxiliaryDataFormats.get(index);
        return new EncryptedSampleImpl(clearSample, entry, cipher);

    }

    private class EncryptedSampleImpl implements Sample {

        private final Sample clearSample;
        private final CencSampleAuxiliaryDataFormat cencSampleAuxiliaryDataFormat;
        private final Cipher cipher;

        private EncryptedSampleImpl(
                Sample clearSample,
                CencSampleAuxiliaryDataFormat cencSampleAuxiliaryDataFormat,
                Cipher cipher) {

            this.clearSample = clearSample;
            this.cencSampleAuxiliaryDataFormat = cencSampleAuxiliaryDataFormat;
            this.cipher = cipher;
        }

        public void writeTo(WritableByteChannel channel) throws IOException {
            ByteBuffer sample = (ByteBuffer) clearSample.asByteBuffer().rewind();
            initCipher(cencSampleAuxiliaryDataFormat.iv);
            try {
                if (cencSampleAuxiliaryDataFormat.pairs != null && cencSampleAuxiliaryDataFormat.pairs.size() > 0) {
                    for (CencSampleAuxiliaryDataFormat.Pair pair : cencSampleAuxiliaryDataFormat.pairs) {
                        byte[] clears = new byte[pair.clear];
                        sample.get(clears);
                        channel.write(ByteBuffer.wrap(clears));
                        if (pair.encrypted > 0) {
                            byte[] toBeEncrypted = new byte[l2i(pair.encrypted)];
                            sample.get(toBeEncrypted);
                            assert (toBeEncrypted.length % 16) == 0;
                            byte[] encrypted = cipher.update(toBeEncrypted);
                            assert encrypted.length == toBeEncrypted.length;
                            channel.write(ByteBuffer.wrap(encrypted));
                        }
                    }
                } else {
                    byte[] fullyEncryptedSample = new byte[sample.limit()];
                    sample.get(fullyEncryptedSample);
                    channel.write(ByteBuffer.wrap(cipher.doFinal(fullyEncryptedSample)));
                }
                sample.rewind();
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            }

        }

        public long getSize() {
            return clearSample.getSize();
        }

        public ByteBuffer asByteBuffer() {

            ByteBuffer sample = (ByteBuffer) clearSample.asByteBuffer().rewind();
            ByteBuffer encSample = ByteBuffer.allocate(sample.limit());

            CencSampleAuxiliaryDataFormat entry = cencSampleAuxiliaryDataFormat;
            initCipher(cencSampleAuxiliaryDataFormat.iv);
            try {
                if (entry.pairs != null) {
                    for (CencSampleAuxiliaryDataFormat.Pair pair : entry.pairs) {
                        byte[] clears = new byte[pair.clear];
                        sample.get(clears);
                        encSample.put(clears);
                        if (pair.encrypted > 0) {
                            byte[] toBeEncrypted = new byte[l2i(pair.encrypted)];
                            sample.get(toBeEncrypted);
                            assert (toBeEncrypted.length % 16) == 0;
                            byte[] encrypted = cipher.update(toBeEncrypted);
                            assert encrypted.length == toBeEncrypted.length;
                            encSample.put(encrypted);
                        }

                    }
                } else {
                    byte[] fullyEncryptedSample = new byte[sample.limit()];
                    sample.get(fullyEncryptedSample);
                    encSample.put(cipher.doFinal(fullyEncryptedSample));
                }
                sample.rewind();
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            }
            encSample.rewind();
            return encSample;
        }
    }

    protected void initCipher(byte[] iv) {
        try {
            byte[] fullIv = new byte[16];
            System.arraycopy(iv, 0, fullIv, 0, iv.length);
            // The IV
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(fullIv));
        } catch (InvalidAlgorithmParameterException e) {
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        return parent.size();
    }

}
