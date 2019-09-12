/*
 * Copyright © 2012, castLabs GmbH, www.castlabs.com
 */

package org.mp4parser.muxer.tracks.encryption;


import org.mp4parser.Container;
import org.mp4parser.boxes.iso14496.part12.SchemeTypeBox;
import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.tracks.encryption.KeyIdKeyPair;
import org.mp4parser.tools.Hex;
import org.mp4parser.tools.Path;
import org.mp4parser.tools.RangeStartMap;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mp4parser.tools.CastUtils.l2i;

public class CencEncryptingSampleList extends AbstractList<Sample> {


    private final RangeStartMap<Integer, SampleEntry> sampleEntries;
    private List<CencSampleAuxiliaryDataFormat> auxiliaryDataFormats;
    private RangeStartMap<Integer, KeyIdKeyPair> keys = new RangeStartMap<>();
    private List<Sample> parent;
    private Map<String, Cipher> ciphers = new HashMap<>();

    public CencEncryptingSampleList(
            RangeStartMap<Integer, KeyIdKeyPair> keys,
            RangeStartMap<Integer, SampleEntry> sampleEntries,
            List<Sample> parent,
            List<CencSampleAuxiliaryDataFormat> auxiliaryDataFormats) {
        this.sampleEntries = sampleEntries;
        this.auxiliaryDataFormats = auxiliaryDataFormats;
        this.keys = keys;
        this.parent = parent;

        try {
            ciphers.put("cenc", Cipher.getInstance("AES/CTR/NoPadding"));
            ciphers.put("cbc1", Cipher.getInstance("AES/CBC/NoPadding"));
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Sample get(int index) {
        Sample clearSample = parent.get(index);
        if (keys.get(index) != null && keys.get(index).getKeyId() != null) {
            return new EncryptedSampleImpl(clearSample, index);
        } else {
            return clearSample;
        }

    }

    private void initCipher(Cipher cipher, byte[] iv, SecretKey cek ) {
        try {
            byte[] fullIv = new byte[16];
            System.arraycopy(iv, 0, fullIv, 0, iv.length);
            // The IV
            cipher.init(Cipher.ENCRYPT_MODE, cek, new IvParameterSpec(fullIv));
        } catch (InvalidAlgorithmParameterException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int size() {
        return parent.size();
    }

    private class EncryptedSampleImpl implements Sample {

        private final Sample clearSample;
        private int index;

        private EncryptedSampleImpl(
                Sample clearSample,
                int index
                ) {

            this.clearSample = clearSample;
            this.index = index;
        }

        public void writeTo(WritableByteChannel channel) throws IOException {

            ByteBuffer sample = (ByteBuffer)((Buffer)clearSample.asByteBuffer()).rewind();
            SampleEntry se = sampleEntries.get(index);
            KeyIdKeyPair keyIdKeyPair = keys.get(index);
            CencSampleAuxiliaryDataFormat entry = auxiliaryDataFormats.get(index);
            SchemeTypeBox schm = Path.getPath((Container) se, "sinf[0]/schm[0]");
            assert schm != null;
            String encryptionAlgo = schm.getSchemeType();
            Cipher cipher = ciphers.get(encryptionAlgo);
            initCipher(cipher, entry.iv, keyIdKeyPair.getKey());
            try {
                if (entry.pairs != null && entry.pairs.length > 0) {
                    byte[] fullSample = new byte[sample.limit()];
                    sample.get(fullSample);
                    int offset = 0;

                    for (CencSampleAuxiliaryDataFormat.Pair pair : entry.pairs) {
                        offset += pair.clear();
                        if (pair.encrypted() > 0) {
                            cipher.update(fullSample,
                                    offset,
                                    l2i(pair.encrypted()),
                                    fullSample,
                                    offset);
                            offset += pair.encrypted();
                        }
                    }
                    channel.write(ByteBuffer.wrap(fullSample));
                } else {
                    byte[] fullyEncryptedSample = new byte[sample.limit()];
                    sample.get(fullyEncryptedSample);
                    if ("cbc1".equals(encryptionAlgo)) {
                        int encryptedLength = fullyEncryptedSample.length / 16 * 16;
                        channel.write(ByteBuffer.wrap(cipher.doFinal(fullyEncryptedSample, 0, encryptedLength)));
                        channel.write(ByteBuffer.wrap(fullyEncryptedSample, encryptedLength, fullyEncryptedSample.length - encryptedLength));
                    } else if ("cenc".equals(encryptionAlgo)) {
                        channel.write(ByteBuffer.wrap(cipher.doFinal(fullyEncryptedSample)));
                    }
                }
                ((Buffer)sample).rewind();
            } catch (IllegalBlockSizeException | BadPaddingException | ShortBufferException e) {
                throw new RuntimeException(e);
            }

        }

        public long getSize() {
            return clearSample.getSize();
        }

        public ByteBuffer asByteBuffer() {
            ByteBuffer sample = (ByteBuffer) ((Buffer)clearSample.asByteBuffer()).rewind();
            ByteBuffer encSample = ByteBuffer.allocate(sample.limit());

            SampleEntry se = sampleEntries.get(index);
            KeyIdKeyPair keyIdKeyPair = keys.get(index);
            CencSampleAuxiliaryDataFormat entry = auxiliaryDataFormats.get(index);
            SchemeTypeBox schm = Path.getPath((Container) se, "sinf[0]/schm[0]");
            assert schm != null;
            String encryptionAlgo = schm.getSchemeType();
            Cipher cipher = ciphers.get(encryptionAlgo);
            initCipher(cipher, entry.iv, keyIdKeyPair.getKey());
            try {
                if (entry.pairs != null) {
                    for (CencSampleAuxiliaryDataFormat.Pair pair : entry.pairs) {
                        byte[] clears = new byte[pair.clear()];
                        sample.get(clears);
                        encSample.put(clears);
                        if (pair.encrypted() > 0) {
                            byte[] toBeEncrypted = new byte[l2i(pair.encrypted())];
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
                    if ("cbc1".equals(encryptionAlgo)) {
                        int encryptedLength = fullyEncryptedSample.length / 16 * 16;
                        encSample.put(cipher.doFinal(fullyEncryptedSample, 0, encryptedLength));
                        encSample.put(fullyEncryptedSample, encryptedLength, fullyEncryptedSample.length - encryptedLength);
                    } else if ("cenc".equals(encryptionAlgo)) {
                        encSample.put(cipher.doFinal(fullyEncryptedSample));
                    }
                }
                ((Buffer)sample).rewind();
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
            ((Buffer)encSample).rewind();
            return encSample;
        }

        @Override
        public SampleEntry getSampleEntry() {
            return sampleEntries.get(index);
        }
    }
}
