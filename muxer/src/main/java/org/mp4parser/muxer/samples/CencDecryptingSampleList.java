package org.mp4parser.muxer.samples;

import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.OriginalFormatBox;
import org.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import org.mp4parser.boxes.iso14496.part12.SchemeTypeBox;
import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.sampleentry.VisualSampleEntry;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.SampleImpl;
import org.mp4parser.muxer.tracks.encryption.KeyIdKeyPair;
import org.mp4parser.tools.ByteBufferByteChannel;
import org.mp4parser.tools.Path;
import org.mp4parser.tools.RangeStartMap;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.AbstractList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import static org.mp4parser.tools.CastUtils.l2i;


public class CencDecryptingSampleList extends AbstractList<Sample> {

    List<CencSampleAuxiliaryDataFormat> sencInfo;
    RangeStartMap<Integer, SecretKey> keys = new RangeStartMap<>();
    List<Sample> parent;


    public CencDecryptingSampleList(SecretKey key, List<Sample> parent, List<CencSampleAuxiliaryDataFormat> sencInfo) {
        this(new RangeStartMap<>(0, key), parent, sencInfo);

    }

    public CencDecryptingSampleList(
            RangeStartMap<Integer, SecretKey> keys,
            List<Sample> parent, List<CencSampleAuxiliaryDataFormat> sencInfo) {
        this.sencInfo = sencInfo;
        this.keys = keys;
        this.parent = parent;
    }

    String getSchemeType(SampleEntry s) {
        SchemeTypeBox schm = Path.getPath((Container) s, "sinf/schm");

        assert schm != null : "Cannot get cipher without schemetypebox";
        return schm.getSchemeType();
    }

    Cipher getCipher(SecretKey sk, byte[] iv, SampleEntry se) {

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
            encSampleBuffer.rewind();
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
                        System.err.println("Decrypted sample but still data remaining: " + encSample.getSize());
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
                encSampleBuffer.rewind();
            } catch (IllegalBlockSizeException | BadPaddingException e) {
                throw new RuntimeException(e);
            }
            decSampleBuffer.rewind();
            return new SampleImpl(decSampleBuffer, decryptSampleEntry(encSample.getSampleEntry()));
        } else {
            return parent.get(index);
        }
    }

    // it's slighty expensive to transform a enca/encv to its original state so cache it.
    private HashMap<SampleEntry, SampleEntry> decryptionCache = new HashMap<>();

    private SampleEntry decryptSampleEntry(SampleEntry se) {
        if (se.getType().startsWith("enc")) {
            SampleEntry decSe = decryptionCache.get(se);
            if (decSe != null) {


                OriginalFormatBox frma;
                if (se.getType().equals("enca")) {
                    frma = Path.getPath((AudioSampleEntry) se, "sinf/frma");
                } else if (se.getType().equals("encv")) {
                    frma = Path.getPath((VisualSampleEntry) se, "sinf/frma");
                } else {
                    throw new RuntimeException("Don't know how to unwrap " + se.getType());
                }
                if (frma == null) {
                    throw new RuntimeException("Could not find frma box");
                }


                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    // This creates a copy cause I can't change the original instance
                    se.getBox(Channels.newChannel(baos));
                    decSe = (SampleEntry) new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray()))).getBoxes().get(0);
                } catch (IOException e) {
                    throw new RuntimeException("Dumping stsd to memory failed");
                }

                if (decSe instanceof AudioSampleEntry) {
                    ((AudioSampleEntry) se).setType(frma.getDataFormat());
                } else if (decSe instanceof VisualSampleEntry) {
                    ((VisualSampleEntry) se).setType(frma.getDataFormat());
                } else {
                    throw new RuntimeException("I don't know " + decSe.getType());
                }

                List<Box> nuBoxes = new LinkedList<Box>();
                for (Box box : decSe.getBoxes()) {
                    if (!box.getType().equals("sinf")) {
                        nuBoxes.add(box);
                    }
                }
                se.setBoxes(nuBoxes);
                decryptionCache.put(se, decSe);
            }
            return decSe;
        }
        return se;
    }

    @Override
    public int size() {
        return parent.size();
    }
}
