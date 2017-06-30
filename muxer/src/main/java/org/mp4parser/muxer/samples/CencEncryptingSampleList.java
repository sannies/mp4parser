/*
 * Copyright © 2012, castLabs GmbH, www.castlabs.com
 */

package org.mp4parser.muxer.samples;


import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.boxes.iso23001.part7.TrackEncryptionBox;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.sampleentry.VisualSampleEntry;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.tracks.encryption.KeyIdKeyPair;
import org.mp4parser.tools.ByteBufferByteChannel;
import org.mp4parser.tools.RangeStartMap;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.mp4parser.tools.CastUtils.l2i;

public class CencEncryptingSampleList extends AbstractList<Sample> {

    private final String encryptionAlgo;
    private Cipher cipher;
    private List<CencSampleAuxiliaryDataFormat> auxiliaryDataFormats;
    private RangeStartMap<Integer, KeyIdKeyPair> keys = new RangeStartMap<>();
    private List<Sample> parent;

    public CencEncryptingSampleList(
            UUID defaultKeyId,
            SecretKey defaultCek,
            List<Sample> parent,
            List<CencSampleAuxiliaryDataFormat> auxiliaryDataFormats,
            String encryptionAlgo) {
        this(new RangeStartMap<>(0, new KeyIdKeyPair(defaultKeyId, defaultCek)), parent, auxiliaryDataFormats, encryptionAlgo);
    }

    public CencEncryptingSampleList(
            RangeStartMap<Integer, KeyIdKeyPair> keys,
            List<Sample> parent,
            List<CencSampleAuxiliaryDataFormat> auxiliaryDataFormats,
            String encryptionAlgo) {
        this.auxiliaryDataFormats = auxiliaryDataFormats;
        this.keys = keys;
        this.encryptionAlgo = encryptionAlgo;
        this.parent = parent;
        try {
            if ("cenc".equals(encryptionAlgo)) {
                this.cipher = Cipher.getInstance("AES/CTR/NoPadding");
            } else if ("cbc1".equals(encryptionAlgo)) {
                this.cipher = Cipher.getInstance("AES/CBC/NoPadding");
            } else {
                throw new RuntimeException("Only cenc & cbc1 is supported as encryptionAlgo");
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Sample get(int index) {
        Sample clearSample = parent.get(index);
        if (keys.get(index) != null && keys.get(index).getKeyId() != null) {
            CencSampleAuxiliaryDataFormat entry = auxiliaryDataFormats.get(index);
            return new EncryptedSampleImpl(clearSample, entry, cipher, keys.get(index).getKey());
        } else {
            return clearSample;
        }

    }

    protected void initCipher(byte[] iv, SecretKey cek) {
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
        private final CencSampleAuxiliaryDataFormat cencSampleAuxiliaryDataFormat;
        private final Cipher cipher;
        private final SecretKey cek;

        private EncryptedSampleImpl(
                Sample clearSample,
                CencSampleAuxiliaryDataFormat cencSampleAuxiliaryDataFormat,
                Cipher cipher,
                SecretKey cek) {

            this.clearSample = clearSample;
            this.cencSampleAuxiliaryDataFormat = cencSampleAuxiliaryDataFormat;
            this.cipher = cipher;
            this.cek = cek;
        }

        public void writeTo(WritableByteChannel channel) throws IOException {
            ByteBuffer sample = (ByteBuffer) clearSample.asByteBuffer().rewind();
            initCipher(cencSampleAuxiliaryDataFormat.iv, cek);
            try {
                if (cencSampleAuxiliaryDataFormat.pairs != null && cencSampleAuxiliaryDataFormat.pairs.length > 0) {
                    byte[] fullSample = new byte[sample.limit()];
                    sample.get(fullSample);
                    int offset = 0;

                    for (CencSampleAuxiliaryDataFormat.Pair pair : cencSampleAuxiliaryDataFormat.pairs) {
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
                sample.rewind();
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            } catch (ShortBufferException e) {
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
            initCipher(cencSampleAuxiliaryDataFormat.iv, cek);
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
                sample.rewind();
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            }
            encSample.rewind();
            return encSample;
        }

        @Override
        public SampleEntry getSampleEntry() {
            SampleEntry encSampleEntry = encryptionCache.get(clearSample.getSampleEntry());
            if (encSampleEntry == null) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    clearSample.getSampleEntry().getBox(Channels.newChannel(baos));
                    encSampleEntry= (SampleEntry) new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray()))).getBoxes().get(0);
                } catch (IOException e) {
                    throw new RuntimeException("Dumping stsd to memory failed");
                }
                // stsd is now a copy of the original stsd. Not very efficient but we don't have to do that a hundred times ...

                OriginalFormatBox originalFormatBox = new OriginalFormatBox();
                originalFormatBox.setDataFormat(clearSample.getSampleEntry().getType());
                ProtectionSchemeInformationBox sinf = new ProtectionSchemeInformationBox();
                sinf.addBox(originalFormatBox);

                SchemeTypeBox schm = new SchemeTypeBox();
                schm.setSchemeType(encryptionAlgo);
                schm.setSchemeVersion(0x00010000);
                sinf.addBox(schm);

                SchemeInformationBox schi = new SchemeInformationBox();
                TrackEncryptionBox trackEncryptionBox = new TrackEncryptionBox();
                trackEncryptionBox.setDefaultIvSize(8);
                trackEncryptionBox.setDefaultAlgorithmId(0x01);
                trackEncryptionBox.setDefault_KID(keys.get(0).getKeyId());
                schi.addBox(trackEncryptionBox);

                sinf.addBox(schi);


                if (clearSample.getSampleEntry() instanceof AudioSampleEntry) {
                    ((AudioSampleEntry) encSampleEntry).setType("enca");
                    ((AudioSampleEntry) encSampleEntry).addBox(sinf);
                } else if (clearSample.getSampleEntry() instanceof VisualSampleEntry) {
                    ((VisualSampleEntry) encSampleEntry).setType("encv");
                    ((VisualSampleEntry) encSampleEntry).addBox(sinf);
                } else {
                    throw new RuntimeException("I don't know how to cenc " + clearSample.getSampleEntry().getType());
                }
                encryptionCache.put(clearSample.getSampleEntry(), encSampleEntry);
            }
            return encSampleEntry;
        }
    }

    private HashMap<SampleEntry, SampleEntry> encryptionCache = new HashMap<>();
}
