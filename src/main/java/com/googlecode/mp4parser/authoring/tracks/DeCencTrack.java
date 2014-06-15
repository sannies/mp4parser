package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.OriginalFormatBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.SchemeTypeBox;
import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.googlecode.mp4parser.authoring.*;
import com.googlecode.mp4parser.boxes.cenc.CencSampleAuxiliaryDataFormat;
import com.googlecode.mp4parser.util.Path;

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
import java.util.LinkedList;
import java.util.List;

public class DeCencTrack extends AbstractTrack {
    DecryptedSampleList samples;
    Track original;


    public DeCencTrack(Mp4TrackImpl original, SecretKey key) {
        this.original = original;
        SchemeTypeBox schm = (SchemeTypeBox) Path.getPath(original.getSampleDescriptionBox(), "enc./sinf/schm");
        if (!"cenc".equals(schm.getSchemeType())) {
            throw new RuntimeException("You can only use the DeCencTrack with CENC encrypted tracks");
        }
        samples = new DecryptedSampleList(key, original.getSamples(), original, original.getSampleEncryptionEntries());
    }


    public SampleDescriptionBox getSampleDescriptionBox() {
        OriginalFormatBox frma = (OriginalFormatBox) Path.getPath(original.getSampleDescriptionBox(), "enc./sinf/frma");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SampleDescriptionBox stsd;
        try {
            original.getSampleDescriptionBox().getBox(Channels.newChannel(baos));
            stsd = (SampleDescriptionBox) new IsoFile(new MemoryDataSourceImpl(baos.toByteArray())).getBoxes().get(0);
        } catch (IOException e) {
            throw new RuntimeException("Dumping stsd to memory failed");
        }

        if (stsd.getSampleEntry() instanceof AudioSampleEntry) {
            ((AudioSampleEntry) stsd.getSampleEntry()).setType(frma.getDataFormat());
        } else if (stsd.getSampleEntry() instanceof VisualSampleEntry) {
            ((VisualSampleEntry) stsd.getSampleEntry()).setType(frma.getDataFormat());
        } else {
            throw new RuntimeException("I don't know " + stsd.getSampleEntry().getType());
        }
        List<Box> nuBoxes = new LinkedList<Box>();
        for (Box box : stsd.getSampleEntry().getBoxes()) {
            if (!box.getType().equals("sinf")) {
                nuBoxes.add(box);
            }
        }
        stsd.getSampleEntry().setBoxes(nuBoxes);
        return stsd;
    }

    public long[] getSampleDurations() {
        return original.getSampleDurations();
    }

    public TrackMetaData getTrackMetaData() {
        return original.getTrackMetaData();
    }

    public String getHandler() {
        return original.getHandler();
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public Box getMediaHeaderBox() {
        return original.getMediaHeaderBox();
    }

    public class DecryptedSampleList extends AbstractList<Sample> {

        List<CencSampleAuxiliaryDataFormat> sencInfo;
        AvcConfigurationBox avcC = null;
        SecretKey secretKey;
        List<Sample> parent;

        public DecryptedSampleList(SecretKey secretKey, List<Sample> parent, Track track, List<CencSampleAuxiliaryDataFormat> sencInfo) {
            this.sencInfo = sencInfo;
            this.secretKey = secretKey;
            this.parent = parent;

            List<Box> boxes = track.getSampleDescriptionBox().getSampleEntry().getBoxes();
            for (Box box : boxes) {
                if (box instanceof AvcConfigurationBox) {
                    avcC = (AvcConfigurationBox) box;
                }
            }
        }


        Cipher getCipher(SecretKey sk, byte[] iv) {
            byte[] fullIv = new byte[16];
            System.arraycopy(iv, 0, fullIv, 0, iv.length);
            // The IV
            try {
                Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");
                cipher.init(Cipher.DECRYPT_MODE, sk, new IvParameterSpec(fullIv));
                return cipher;
            } catch (InvalidAlgorithmParameterException e) {
                throw new RuntimeException(e);
            } catch (InvalidKeyException e) {
                throw new RuntimeException(e);
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            } catch (NoSuchPaddingException e) {
                throw new RuntimeException(e);
            }


        }

        @Override
        public Sample get(int index) {
            Sample encSample = parent.get(index);
            final ByteBuffer encSampleBuffer = encSample.asByteBuffer();
            encSampleBuffer.rewind();
            final ByteBuffer decSampleBuffer = ByteBuffer.allocate(encSampleBuffer.limit());
            final CencSampleAuxiliaryDataFormat sencEntry = sencInfo.get(index);
            Cipher cipher = getCipher(secretKey, sencEntry.iv);
            try {
                if (avcC != null) {
                    final List<CencSampleAuxiliaryDataFormat.Pair> pairs = sencEntry.pairs;
                    for (CencSampleAuxiliaryDataFormat.Pair pair : pairs) {
                        final int clearBytes = pair.clear;
                        final int encrypted = (int) pair.encrypted;

                        byte[] clears = new byte[clearBytes];
                        encSampleBuffer.get(clears);
                        decSampleBuffer.put(clears);

                        byte[] encs = new byte[encrypted];
                        encSampleBuffer.get(encs);
                        final byte[] decr = cipher.doFinal(encs);
                        decSampleBuffer.put(decr);

                    }
                    if (encSampleBuffer.remaining() > 0) {
                        System.err.println("Decrypted sample but still data remaining: " + encSample.getSize());
                    }
                    decSampleBuffer.put(cipher.doFinal());
                } else {
                    byte[] fullyEncryptedSample = new byte[encSampleBuffer.limit()];
                    encSampleBuffer.get(fullyEncryptedSample);
                    decSampleBuffer.put(cipher.doFinal(fullyEncryptedSample));
                }
                encSampleBuffer.rewind();
            } catch (IllegalBlockSizeException e) {
                throw new RuntimeException(e);
            } catch (BadPaddingException e) {
                throw new RuntimeException(e);
            }
            decSampleBuffer.rewind();
            return new SampleImpl(decSampleBuffer);
        }

        @Override
        public int size() {
            return parent.size();
        }
    }

}
