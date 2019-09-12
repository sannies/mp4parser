package org.mp4parser.muxer.tracks.encryption;

import org.mp4parser.Box;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.boxes.iso14496.part15.AvcConfigurationBox;
import org.mp4parser.boxes.iso14496.part15.HevcConfigurationBox;
import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.samplegrouping.GroupEntry;
import org.mp4parser.muxer.Edit;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.TrackMetaData;
import org.mp4parser.muxer.tracks.h264.H264NalUnitHeader;
import org.mp4parser.muxer.tracks.h264.H264NalUnitTypes;
import org.mp4parser.muxer.tracks.h264.H264TrackImpl;
import org.mp4parser.muxer.tracks.h265.H265NalUnitHeader;
import org.mp4parser.muxer.tracks.h265.H265NalUnitTypes;
import org.mp4parser.muxer.tracks.h265.H265TrackImpl;
import org.mp4parser.tools.IsoTypeReaderVariable;
import org.mp4parser.tools.RangeStartMap;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.*;

import static org.mp4parser.tools.CastUtils.l2i;

/**
 * Encrypts a given track with common encryption.
 */
public class CencEncryptingTrackImpl implements CencEncryptedTrack {

    private Track source;
    private CencEncryptingSampleList samples;
    private List<CencSampleAuxiliaryDataFormat> cencSampleAuxiliaryData;
    private LinkedHashSet<SampleEntry> sampleEntries = new LinkedHashSet<>();
    private boolean subSampleEncryption;
    private Object configurationBox;
    private Map<GroupEntry, long[]> groupEntries = new HashMap<>();

    public CencEncryptingTrackImpl(Track source, UUID defaultKeyId, SecretKey key, boolean dummyIvs) {
        this(source, new RangeStartMap<>(0, defaultKeyId), Collections.singletonMap(defaultKeyId, key),
                "cenc", dummyIvs, false);
    }


    /**
     * Encrypts a given source track.
     *
     * @param source             unencrypted source file
     * @param indexToKeyId      dunno
     * @param keys               key ID to key map
     * @param encryptionAlgo     cenc or cbc1 (don't use cbc1)
     * @param dummyIvs           disables RNG for IVs and use IVs starting with 0x00...000
     * @param encryptButAllClear will cause sub sample encryption format to keep full sample in clear (clear/encrypted pair will be len(sample)/0
     */
    public CencEncryptingTrackImpl(Track source, RangeStartMap<Integer, UUID> indexToKeyId, Map<UUID, SecretKey> keys,
                                   String encryptionAlgo, boolean dummyIvs, boolean encryptButAllClear) {
        this.source = source;
        this.cencSampleAuxiliaryData = new ArrayList<>();

        BigInteger one = new BigInteger("1");
        byte[] init = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};

        if (!dummyIvs) {
            Random random = new SecureRandom();
            random.nextBytes(init);
        }
        BigInteger ivInt = new BigInteger(1, init);

        CencEncryptingSampleEntryTransformer tx = new CencEncryptingSampleEntryTransformer();

        Map<SampleEntry, Integer> nalLengthSizes = new HashMap<>();
        for (SampleEntry sampleEntry : source.getSampleEntries()) {

            List<Box> boxes = sampleEntry.getBoxes();

            for (Box box : boxes) {
                if (box instanceof AvcConfigurationBox) {
                    AvcConfigurationBox avcC = (AvcConfigurationBox) (configurationBox = box);
                    nalLengthSizes.put(sampleEntry, avcC.getLengthSizeMinusOne() + 1);
                    subSampleEncryption= true;
                } else if (box instanceof HevcConfigurationBox) {
                    HevcConfigurationBox hvcC = (HevcConfigurationBox) (configurationBox = box);
                    nalLengthSizes.put(sampleEntry, hvcC.getLengthSizeMinusOne() + 1);
                    subSampleEncryption= true;
                } else {
                    if (!nalLengthSizes.containsKey(sampleEntry)) {
                        nalLengthSizes.put(sampleEntry, -1);
                    }
                }

            }
        }

        List<Sample> sourceSamples = source.getSamples();
        RangeStartMap<Integer, SampleEntry> indexToSampleEntry = new RangeStartMap<>();
        RangeStartMap<Integer, KeyIdKeyPair> indexToKey = new RangeStartMap<>();
        SampleEntry previousSampleEntry = null;
        for (int i = 0; i < sourceSamples.size(); i++) {
            Sample origSample = sourceSamples.get(i);
            int nalLengthSize = nalLengthSizes.get(origSample.getSampleEntry());
            CencSampleAuxiliaryDataFormat e = new CencSampleAuxiliaryDataFormat();
            this.cencSampleAuxiliaryData.add(e);
            UUID keyId = indexToKeyId.get(i);
            if (keyId != null) {
                SampleEntry correct = tx.transform(origSample.getSampleEntry(), encryptionAlgo, indexToKeyId.get(i));
                sampleEntries.add(correct);
                if (previousSampleEntry != correct) {
                    indexToSampleEntry.put(i, correct);
                    indexToKey.put(i, new KeyIdKeyPair(keyId, keys.get(indexToKeyId.get(i))));
                }
                previousSampleEntry = correct;

                byte[] iv = ivInt.toByteArray();
                byte[] eightByteIv = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
                System.arraycopy(
                        iv,
                        iv.length - 8 > 0 ? iv.length - 8 : 0,
                        eightByteIv,
                        (8 - iv.length) < 0 ? 0 : (8 - iv.length),
                        iv.length > 8 ? 8 : iv.length);

                e.iv = eightByteIv;

                ByteBuffer sample = (ByteBuffer) ((Buffer)origSample.asByteBuffer()).rewind();

                if (nalLengthSize > 0) {
                    if (encryptButAllClear) {
                        e.pairs = new CencSampleAuxiliaryDataFormat.Pair[]{e.createPair(sample.remaining(), 0)};
                    } else {
                        List<CencSampleAuxiliaryDataFormat.Pair> pairs = new ArrayList<>(5);
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
                            ((Buffer)sample).position(sample.position() + nalLength);
                        }
                        e.pairs = pairs.toArray(new CencSampleAuxiliaryDataFormat.Pair[pairs.size()]);
                    }
                }

                ivInt = ivInt.add(one);
            } else {

                SampleEntry correct = origSample.getSampleEntry();
                sampleEntries.add(correct);
                if (previousSampleEntry != correct) {
                    indexToSampleEntry.put(i, correct);
                    indexToKey.put(i, null);
                }
                previousSampleEntry = correct;
            }
        }

        this.samples = new CencEncryptingSampleList(indexToKey, indexToSampleEntry, source.getSamples(), cencSampleAuxiliaryData);
    }

    public boolean hasSubSampleEncryption() {
        return subSampleEncryption;
    }

    public List<CencSampleAuxiliaryDataFormat> getSampleEncryptionEntries() {
        return cencSampleAuxiliaryData;
    }

    public synchronized List<SampleEntry> getSampleEntries() {
        return new ArrayList<>(sampleEntries);
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

    @Override
    public Map<GroupEntry, long[]> getSampleGroups() {
        return groupEntries;
    }

    private boolean isClearNal(ByteBuffer s) {
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
