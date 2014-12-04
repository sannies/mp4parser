package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReaderVariable;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.googlecode.mp4parser.authoring.Edit;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.boxes.cenc.CencEncryptingSampleList;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.CencSampleEncryptionInformationGroupEntry;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.GroupEntry;
import com.googlecode.mp4parser.util.RangeStartMap;
import com.mp4parser.iso14496.part15.AvcConfigurationBox;
import com.mp4parser.iso23001.part7.CencSampleAuxiliaryDataFormat;
import com.mp4parser.iso23001.part7.TrackEncryptionBox;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.security.SecureRandom;
import java.util.*;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Encrypts a given track with common encryption.
 */
public class CencEncryptingTrackImpl implements CencEncryptedTrack {
    private final String encryptionAlgo;
    Track source;
    Map<UUID, SecretKey> keys = new HashMap<UUID, SecretKey>();
    UUID defaultKeyId;
    List<Sample> samples;
    List<CencSampleAuxiliaryDataFormat> cencSampleAuxiliaryData;
    boolean dummyIvs = false;
    boolean subSampleEncryption = false;
    SampleDescriptionBox stsd = null;

    RangeStartMap<Integer, SecretKey> indexToKey;
    Map<GroupEntry, long[]> sampleGroups;

    public CencEncryptingTrackImpl(Track source, UUID defaultKeyId, SecretKey key, boolean dummyIvs) {
        this(source, defaultKeyId, Collections.singletonMap(defaultKeyId, key),
                null,
                "cenc", dummyIvs);
    }

    public CencEncryptingTrackImpl(Track source, UUID defaultKeyId, Map<UUID, SecretKey> keys,
                                   Map<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation,
                                   String encryptionAlgo, boolean dummyIvs) {
        this.source = source;
        this.keys = keys;
        this.defaultKeyId = defaultKeyId;
        this.dummyIvs = dummyIvs;
        this.encryptionAlgo = encryptionAlgo;
        this.sampleGroups = new HashMap<GroupEntry, long[]>();
        for (Map.Entry<GroupEntry, long[]> entry : source.getSampleGroups().entrySet()) {
            if (!(entry.getKey() instanceof CencSampleEncryptionInformationGroupEntry)) {
                sampleGroups.put(entry.getKey(), entry.getValue());
            }
        }
        if (keyRotation != null) {
            for (Map.Entry<CencSampleEncryptionInformationGroupEntry, long[]> entry : keyRotation.entrySet()) {
                sampleGroups.put(entry.getKey(), entry.getValue());
            }
        }
        this.sampleGroups = new HashMap<GroupEntry, long[]>(sampleGroups) {
            @Override
            public long[] put(GroupEntry key, long[] value) {
                if (key instanceof CencSampleEncryptionInformationGroupEntry) {
                    throw new RuntimeException("Please supply CencSampleEncryptionInformationGroupEntries in the constructor");
                }
                return super.put(key, value);
            }
        };


        this.samples = source.getSamples();
        this.cencSampleAuxiliaryData = new ArrayList<CencSampleAuxiliaryDataFormat>();

        BigInteger one = new BigInteger("1");
        byte[] init = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

        if (!dummyIvs) {
            Random random = new SecureRandom();
            random.nextBytes(init);
        }
        BigInteger ivInt = new BigInteger(1, init);

        List<CencSampleEncryptionInformationGroupEntry> groupEntries =
                new ArrayList<CencSampleEncryptionInformationGroupEntry>();
        if (keyRotation != null) {
            groupEntries.addAll(keyRotation.keySet());
        }
        indexToKey = new RangeStartMap<Integer, SecretKey>();
        int lastSampleGroupDescriptionIndex = -1;
        for (int i = 0; i < source.getSamples().size(); i++) {
            int index = 0;
            for (int j = 0; j < groupEntries.size(); j++) {
                GroupEntry groupEntry = groupEntries.get(j);
                long[] sampleNums = getSampleGroups().get(groupEntry);
                if (Arrays.binarySearch(sampleNums, i) >= 0) {
                    index = j + 1;
                }
            }
            if (lastSampleGroupDescriptionIndex != index) {
                if (index == 0) {
                    indexToKey.put(i, keys.get(defaultKeyId));
                } else {
                    if (groupEntries.get(index - 1).getKid() != null) {
                        SecretKey sk = keys.get(groupEntries.get(index - 1).getKid());
                        if (sk == null) {
                            throw new RuntimeException("Key " + groupEntries.get(index - 1).getKid() + " was not supplied for decryption");
                        }
                        indexToKey.put(i, sk);
                    } else {
                        indexToKey.put(i, null);
                    }
                }
                lastSampleGroupDescriptionIndex = index;

            }
        }


        AvcConfigurationBox avcC = null;
        List<Box> boxes = source.getSampleDescriptionBox().getSampleEntry().getBoxes();
        for (Box box : boxes) {
            if (box instanceof AvcConfigurationBox) {
                avcC = (AvcConfigurationBox) box;
                subSampleEncryption = true;
            }
        }


        for (int i = 0; i < samples.size(); i++) {
            Sample origSample = samples.get(i);
            CencSampleAuxiliaryDataFormat e = new CencSampleAuxiliaryDataFormat();
            this.cencSampleAuxiliaryData.add(e);
            if (indexToKey.get(i) != null) {
                byte[] iv = ivInt.toByteArray();
                byte[] eightByteIv = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
                System.arraycopy(
                        iv,
                        iv.length - 8 > 0 ? iv.length - 8 : 0,
                        eightByteIv,
                        (8 - iv.length) < 0 ? 0 : (8 - iv.length),
                        iv.length > 8 ? 8 : iv.length);

                e.iv = eightByteIv;

                ByteBuffer sample = (ByteBuffer) origSample.asByteBuffer().rewind();


                if (avcC != null) {
                    int nalLengthSize = avcC.getLengthSizeMinusOne() + 1;
                    List<CencSampleAuxiliaryDataFormat.Pair> pairs = new ArrayList<CencSampleAuxiliaryDataFormat.Pair>(5);
                    while (sample.remaining() > 0) {
                        int nalLength = l2i(IsoTypeReaderVariable.read(sample, nalLengthSize));
                        int clearBytes;
                        int nalGrossSize = nalLength + nalLengthSize;
                        if (nalGrossSize >= 112) {
                            clearBytes = 96 + nalGrossSize % 16;
                        } else {
                            clearBytes = nalGrossSize;
                        }
                        pairs.add(e.createPair(clearBytes, nalGrossSize - clearBytes));
                        sample.position(sample.position() + nalLength);
                    }
                    e.pairs = pairs.toArray(new CencSampleAuxiliaryDataFormat.Pair[pairs.size()]);
                }

                ivInt = ivInt.add(one);
            }
        }

        System.err.println("");
    }

    public UUID getDefaultKeyId() {
        return defaultKeyId;
    }

    public boolean hasSubSampleEncryption() {
        return subSampleEncryption;
    }

    public List<CencSampleAuxiliaryDataFormat> getSampleEncryptionEntries() {
        return cencSampleAuxiliaryData;
    }

    public synchronized SampleDescriptionBox getSampleDescriptionBox() {
        if (stsd == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
            schm.setSchemeType(encryptionAlgo);
            schm.setSchemeVersion(0x00010000);
            sinf.addBox(schm);

            SchemeInformationBox schi = new SchemeInformationBox();
            TrackEncryptionBox trackEncryptionBox = new TrackEncryptionBox();
            trackEncryptionBox.setDefaultIvSize(defaultKeyId == null ? 0 : 8);
            trackEncryptionBox.setDefaultAlgorithmId(defaultKeyId == null ? 0x0 : 0x01);
            trackEncryptionBox.setDefault_KID(defaultKeyId == null ? new UUID(0, 0) : defaultKeyId);
            schi.addBox(trackEncryptionBox);

            sinf.addBox(schi);
            stsd.getSampleEntry().addBox(sinf);
        }
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
        return new CencEncryptingSampleList(indexToKey, source.getSamples(), cencSampleAuxiliaryData, encryptionAlgo);
    }

    public SubSampleInformationBox getSubsampleInformationBox() {
        return source.getSubsampleInformationBox();
    }

    public void close() throws IOException {
        source.close();
    }

    public String getName() {
        return "enc(" + source.getName() + ")";
    }

    public List<Edit> getEdits() {
        return source.getEdits();
    }

    public Map<GroupEntry, long[]> getSampleGroups() {
        return sampleGroups;
    }
}
