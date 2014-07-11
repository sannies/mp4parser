package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReaderVariable;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.h264.AvcConfigurationBox;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.boxes.basemediaformat.TrackEncryptionBox;
import com.googlecode.mp4parser.boxes.cenc.CencEncryptingSampleList;
import com.googlecode.mp4parser.boxes.cenc.CencSampleAuxiliaryDataFormat;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.security.SecureRandom;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Encrypts a given track with common encryption.
 */
public class CencEncryptingTrackImpl implements CencEncyprtedTrack {
    Track source;
    SecretKey cek;
    UUID keyId;
    CencEncryptingSampleList samples;
    List<CencSampleAuxiliaryDataFormat> cencSampleAuxiliaryData;
    private Random random = new SecureRandom();
    boolean dummyIvs = false;
    boolean subSampleEncryption = false;

    public CencEncryptingTrackImpl(Track source, UUID keyId, SecretKey cek) {
        this(source, keyId, cek, null);
    }


    public CencEncryptingTrackImpl(Track source, UUID keyId, SecretKey cek, List<CencSampleAuxiliaryDataFormat> cencSampleAuxiliaryData) {
        this.source = source;
        this.cek = cek;
        this.keyId = keyId;
        List<Sample> origSamples = source.getSamples();

        if (cencSampleAuxiliaryData == null) {
            this.cencSampleAuxiliaryData = cencSampleAuxiliaryData  = new LinkedList<CencSampleAuxiliaryDataFormat>();

            BigInteger one = new BigInteger("1");
            byte[] init = new byte[]{};
            BigInteger ivInt = new BigInteger(1, new byte[]{0, 0, 0, 0, 0, 0, 0, 0});
            if (!dummyIvs) {
                random.nextBytes(init);
            }


            AvcConfigurationBox avcC = null;
            List<Box> boxes = source.getSampleDescriptionBox().getSampleEntry().getBoxes();
            for (Box box : boxes) {
                if (box instanceof AvcConfigurationBox) {
                    avcC = (AvcConfigurationBox) box;
                    subSampleEncryption = true;
                }
            }


            for (Sample origSample : origSamples) {
                byte[] iv = ivInt.toByteArray();
                byte[] eightByteIv = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
                System.arraycopy(
                        iv,
                        iv.length - 8 > 0 ? iv.length - 8 : 0,
                        eightByteIv,
                        (8 - iv.length) < 0 ? 0 : (8 - iv.length),
                        iv.length > 8 ? 8 : iv.length);

                CencSampleAuxiliaryDataFormat e = new CencSampleAuxiliaryDataFormat();
                this.cencSampleAuxiliaryData.add(e);

                e.iv = eightByteIv;

                ByteBuffer sample = (ByteBuffer) origSample.asByteBuffer().rewind();


                if (avcC != null) {
                    int nalLengthSize = avcC.getLengthSizeMinusOne() + 1;
                    while (sample.remaining() > 0) {
                        int nalLength = l2i(IsoTypeReaderVariable.read(sample, nalLengthSize));
                        int clearBytes;
                        int nalGrossSize = nalLength + nalLengthSize;
                        if (nalGrossSize >= 112) {
                            clearBytes = 96 + nalGrossSize % 16;
                        } else {
                            clearBytes = nalGrossSize;
                        }
                        e.pairs.add(e.createPair(clearBytes, nalGrossSize - clearBytes));
                        sample.position(sample.position() + nalLength);
                    }
                }

                ivInt = ivInt.add(one);
            }
        }

        samples = new CencEncryptingSampleList(cek, source.getSamples(), cencSampleAuxiliaryData);
    }

    public void setDummyIvs(boolean dummyIvs) {
        this.dummyIvs = dummyIvs;
    }

    public UUID getKeyId() {
        return keyId;
    }

    public boolean hasSubSampleEncryption() {
        return subSampleEncryption;
    }

    public List<CencSampleAuxiliaryDataFormat> getSampleEncryptionEntries() {
        return cencSampleAuxiliaryData;
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SampleDescriptionBox stsd;
        try {
            source.getSampleDescriptionBox().getBox(Channels.newChannel(baos));
            stsd = (SampleDescriptionBox) new IsoFile(new MemoryDataSourceImpl(baos.toByteArray())).getBoxes().get(0);
        } catch (IOException e) {
            throw new RuntimeException("Dumping stsd to memory failed");
        }
        // stsd is now a copy of the original stsd. Not very efficient but we don't have to do that a hundred times ...

        OriginalFormatBox originalFormatBox = new OriginalFormatBox();
        originalFormatBox.setDataFormat(stsd.getSampleEntry().getType());

        if (stsd.getSampleEntry() instanceof AudioSampleEntry) {
            ((AudioSampleEntry) stsd.getSampleEntry()).setType("enca");
        } else if (stsd.getSampleEntry() instanceof VisualSampleEntry) {
            ((VisualSampleEntry) stsd.getSampleEntry()).setType("encv");
        } else {
            throw new RuntimeException("I don't know how to cenc " + stsd.getSampleEntry().getType());
        }
        ProtectionSchemeInformationBox sinf = new ProtectionSchemeInformationBox();
        sinf.addBox(originalFormatBox);

        SchemeTypeBox schm = new SchemeTypeBox();
        schm.setSchemeType("cenc");
        schm.setSchemeVersion(0);
        sinf.addBox(schm);

        SchemeInformationBox schi = new SchemeInformationBox();
        TrackEncryptionBox trackEncryptionBox = new TrackEncryptionBox();
        trackEncryptionBox.setDefaultIvSize(8);
        trackEncryptionBox.setDefaultAlgorithmId(0x01);
        trackEncryptionBox.setDefault_KID(keyId);
        schi.addBox(trackEncryptionBox);

        sinf.addBox(schi);
        stsd.getSampleEntry().addBox(sinf);

        return stsd;
    }

    public long[] getSampleDurations() {
        return source.getSampleDurations();
    }

    public long getDuration() {
        return source.getDuration();
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return source.getCompositionTimeEntries();
    }

    public long[] getSyncSamples() {
        return source.getSyncSamples();
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return source.getSampleDependencies();
    }

    public TrackMetaData getTrackMetaData() {
        return source.getTrackMetaData();
    }

    public String getHandler() {
        return source.getHandler();
    }

    public List<Sample> getSamples() {
        return samples;
    }

    public Box getMediaHeaderBox() {
        return source.getMediaHeaderBox();
    }

    public SubSampleInformationBox getSubsampleInformationBox() {
        return source.getSubsampleInformationBox();
    }

    public void close() throws IOException {
        source.close();
    }
}
