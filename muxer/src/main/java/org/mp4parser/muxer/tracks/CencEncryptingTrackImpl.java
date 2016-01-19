package org.mp4parser.muxer.tracks;

import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.boxes.iso14496.part15.AvcConfigurationBox;
import org.mp4parser.boxes.iso14496.part15.HevcConfigurationBox;
import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.boxes.iso23001.part7.TrackEncryptionBox;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.mp4parser.boxes.sampleentry.VisualSampleEntry;
import org.mp4parser.boxes.samplegrouping.CencSampleEncryptionInformationGroupEntry;
import org.mp4parser.boxes.samplegrouping.GroupEntry;
import org.mp4parser.muxer.Edit;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.TrackMetaData;
import org.mp4parser.muxer.samples.CencEncryptingSampleList;
import org.mp4parser.muxer.tracks.h264.H264NalUnitHeader;
import org.mp4parser.muxer.tracks.h264.H264NalUnitTypes;
import org.mp4parser.muxer.tracks.h264.H264TrackImpl;
import org.mp4parser.muxer.tracks.h265.H265NalUnitHeader;
import org.mp4parser.muxer.tracks.h265.H265NalUnitTypes;
import org.mp4parser.muxer.tracks.h265.H265TrackImpl;
import org.mp4parser.tools.ByteBufferByteChannel;
import org.mp4parser.tools.IsoTypeReaderVariable;
import org.mp4parser.tools.RangeStartMap;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.security.SecureRandom;
import java.util.*;

import static org.mp4parser.tools.CastUtils.l2i;

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

    Object configurationBox;

    public CencEncryptingTrackImpl(Track source, UUID defaultKeyId, SecretKey key, boolean dummyIvs) {
        this(source, defaultKeyId, Collections.singletonMap(defaultKeyId, key),
                null,
                "cenc", dummyIvs);
    }

    public CencEncryptingTrackImpl(Track source, UUID defaultKeyId, Map<UUID, SecretKey> keys,
                                   Map<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation,
                                   String encryptionAlgo, boolean dummyIvs) {
        this(source, defaultKeyId, keys, keyRotation, encryptionAlgo, dummyIvs, false);

    }

    /**
     * Encrypts a given source track.
     *
     * @param source             unencrypted source file
     * @param defaultKeyId       the default key ID - might be null if sample are not encrypted by default
     * @param keys               key ID to key map
     * @param keyRotation        assigns an encryption group to a number of samples
     * @param encryptionAlgo     cenc or cbc1 (don't use cbc1)
     * @param dummyIvs           disables RNG for IVs and use IVs starting with 0x00...000
     * @param encryptButAllClear will cause sub sample encryption format to keep full sample in clear (clear/encrypted pair will be len(sample)/0
     */
    public CencEncryptingTrackImpl(Track source, UUID defaultKeyId, Map<UUID, SecretKey> keys,
                                   Map<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation,
                                   String encryptionAlgo, boolean dummyIvs, boolean encryptButAllClear) {
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


        List<Box> boxes = source.getSampleDescriptionBox().getSampleEntry().getBoxes();
        int nalLengthSize = -1;
        for (Box box : boxes) {
            if (box instanceof AvcConfigurationBox) {
                AvcConfigurationBox avcC = (AvcConfigurationBox) (configurationBox = box);
                subSampleEncryption = true;
                nalLengthSize = avcC.getLengthSizeMinusOne() + 1;
            }
            if (box instanceof HevcConfigurationBox) {
                HevcConfigurationBox hvcC = (HevcConfigurationBox) (configurationBox = box);
                subSampleEncryption = true;
                nalLengthSize = hvcC.getLengthSizeMinusOne() + 1;
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


                if (subSampleEncryption) {
                    if (encryptButAllClear) {
                        e.pairs = new CencSampleAuxiliaryDataFormat.Pair[]{e.createPair(sample.remaining(), 0)};
                    } else {
                        List<CencSampleAuxiliaryDataFormat.Pair> pairs = new ArrayList<CencSampleAuxiliaryDataFormat.Pair>(5);
                        while (sample.remaining() > 0) {
                            int nalLength = l2i(IsoTypeReaderVariable.read(sample, nalLengthSize));
                            int clearBytes;
                            int nalGrossSize = nalLength + nalLengthSize;
                            if (nalGrossSize < 112 || isClearNal(sample.duplicate())) {
                                clearBytes = nalGrossSize;
                            } else {
                                clearBytes = 96 + nalGrossSize % 16;
                            }
                            pairs.add(e.createPair(clearBytes, nalGrossSize - clearBytes));
                            sample.position(sample.position() + nalLength);
                        }
                        e.pairs = pairs.toArray(new CencSampleAuxiliaryDataFormat.Pair[pairs.size()]);
                    }
                }

                ivInt = ivInt.add(one);
            }
        }
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
                stsd = (SampleDescriptionBox) new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray()))).getBoxes().get(0);
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

    public boolean isClearNal(ByteBuffer s) {
        if (configurationBox instanceof HevcConfigurationBox) {
            H265NalUnitHeader nuh = H265TrackImpl.getNalUnitHeader(s.slice());
            return !( // These ranges are all slices --> NOT CLEAR
                    (nuh.nalUnitType >= H265NalUnitTypes.NAL_TYPE_TRAIL_N && (nuh.nalUnitType <= H265NalUnitTypes.NAL_TYPE_RASL_R)) ||
                            (nuh.nalUnitType >= H265NalUnitTypes.NAL_TYPE_BLA_W_LP && (nuh.nalUnitType <= H265NalUnitTypes.NAL_TYPE_CRA_NUT)) ||
                            (nuh.nalUnitType >= H265NalUnitTypes.NAL_TYPE_BLA_W_LP && (nuh.nalUnitType <= H265NalUnitTypes.NAL_TYPE_CRA_NUT))
            );
        } else if (configurationBox instanceof AvcConfigurationBox) {
            // only encrypt
            H264NalUnitHeader nuh = H264TrackImpl.getNalUnitHeader(s.slice());
            return !(nuh.nal_unit_type == H264NalUnitTypes.CODED_SLICE_AUX_PIC ||
                    nuh.nal_unit_type == H264NalUnitTypes.CODED_SLICE_DATA_PART_A ||
                    nuh.nal_unit_type == H264NalUnitTypes.CODED_SLICE_DATA_PART_B ||
                    nuh.nal_unit_type == H264NalUnitTypes.CODED_SLICE_DATA_PART_C ||
                    nuh.nal_unit_type == H264NalUnitTypes.CODED_SLICE_EXT ||
                    nuh.nal_unit_type == H264NalUnitTypes.CODED_SLICE_IDR ||
                    nuh.nal_unit_type == H264NalUnitTypes.CODED_SLICE_NON_IDR
            );

        } else {
            throw new RuntimeException("Subsample encryption is activated but the CencEncryptingTrackImpl can't say if this sample is to be encrypted or not!");
        }
    }


}
