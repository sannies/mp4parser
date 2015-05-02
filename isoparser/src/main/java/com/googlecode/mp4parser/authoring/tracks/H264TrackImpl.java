package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.h264.model.PictureParameterSet;
import com.googlecode.mp4parser.h264.model.SeqParameterSet;
import com.googlecode.mp4parser.h264.read.CAVLCReader;
import com.googlecode.mp4parser.util.RangeStartMap;
import com.mp4parser.iso14496.part15.AvcConfigurationBox;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.logging.Logger;

/**
 * The <code>H264TrackImpl</code> creates a <code>Track</code> from an H.264
 * Annex B file.
 */
public class H264TrackImpl extends AbstractH26XTrack {
    private static final Logger LOG = Logger.getLogger(H264TrackImpl.class.getName());

    Map<Integer, byte[]> spsIdToSpsBytes = new HashMap<Integer, byte[]>();
    Map<Integer, SeqParameterSet> spsIdToSps = new HashMap<Integer, SeqParameterSet>();
    Map<Integer, byte[]> ppsIdToPpsBytes = new HashMap<Integer, byte[]>();
    Map<Integer, PictureParameterSet> ppsIdToPps = new HashMap<Integer, PictureParameterSet>();

    SampleDescriptionBox sampleDescriptionBox;

    private List<Sample> samples;


    SeqParameterSet firstSeqParameterSet = null;
    PictureParameterSet firstPictureParameterSet = null;

    SeqParameterSet currentSeqParameterSet = null;
    PictureParameterSet currentPictureParameterSet = null;

    RangeStartMap<Integer, byte[]> seqParameterRangeMap = new RangeStartMap<Integer, byte[]>();
    RangeStartMap<Integer, byte[]> pictureParameterRangeMap = new RangeStartMap<Integer, byte[]>();

    private int width;
    private int height;
    private long timescale;
    private int frametick;


    private SEIMessage seiMessage;
    int frameNrInGop = 0;
    private boolean determineFrameRate = true;
    private String lang = "eng";





    /**
     * Creates a new <code>Track</code> object from a raw H264 source (<code>DataSource dataSource1</code>).
     * Whenever the timescale and frametick are set to negative value (e.g. -1) the H264TrackImpl
     * tries to detect the frame rate.
     * Typically values for <code>timescale</code> and <code>frametick</code> are:
     * <ul>
     * <li>23.976 FPS: timescale = 24000; frametick = 1001</li>
     * <li>25 FPS: timescale = 25; frametick = 1</li>
     * <li>29.97 FPS: timescale = 30000; frametick = 1001</li>
     * <li>30 FPS: timescale = 30; frametick = 1</li>
     * </ul>
     *
     * @param dataSource the source file of the H264 samples
     * @param lang       language of the movie (in doubt: use "eng")
     * @param timescale  number of time units (ticks) in one second
     * @param frametick  number of time units (ticks) that pass while showing exactly one frame
     * @throws IOException in case of problems whiel reading from the <code>DataSource</code>
     */
    public H264TrackImpl(DataSource dataSource, String lang, long timescale, int frametick) throws IOException {
        super(dataSource);
        this.lang = lang;
        this.timescale = timescale; //e.g. 23976
        this.frametick = frametick;
        if ((timescale > 0) && (frametick > 0)) {
            this.determineFrameRate = false;
        }

        parse(new LookAhead(dataSource));
    }



    public H264TrackImpl(DataSource dataSource, String lang) throws IOException {
        this(dataSource, lang, -1, -1);
    }

    public H264TrackImpl(DataSource dataSource) throws IOException {
        this(dataSource, "eng");
    }

    private void parse(LookAhead la) throws IOException {



        samples = new LinkedList<Sample>();
        if (!readSamples(la)) {
            throw new IOException();
        }

        if (!readVariables()) {
            throw new IOException();
        }

        sampleDescriptionBox = new SampleDescriptionBox();
        VisualSampleEntry visualSampleEntry = new VisualSampleEntry("avc1");
        visualSampleEntry.setDataReferenceIndex(1);
        visualSampleEntry.setDepth(24);
        visualSampleEntry.setFrameCount(1);
        visualSampleEntry.setHorizresolution(72);
        visualSampleEntry.setVertresolution(72);
        visualSampleEntry.setWidth(width);
        visualSampleEntry.setHeight(height);
        visualSampleEntry.setCompressorname("AVC Coding");

        AvcConfigurationBox avcConfigurationBox = new AvcConfigurationBox();

        avcConfigurationBox.setSequenceParameterSets(new ArrayList<byte[]>(spsIdToSpsBytes.values()));
        avcConfigurationBox.setPictureParameterSets(new ArrayList<byte[]>(ppsIdToPpsBytes.values()));
        avcConfigurationBox.setAvcLevelIndication(firstSeqParameterSet.level_idc);
        avcConfigurationBox.setAvcProfileIndication(firstSeqParameterSet.profile_idc);
        avcConfigurationBox.setBitDepthLumaMinus8(firstSeqParameterSet.bit_depth_luma_minus8);
        avcConfigurationBox.setBitDepthChromaMinus8(firstSeqParameterSet.bit_depth_chroma_minus8);
        avcConfigurationBox.setChromaFormat(firstSeqParameterSet.chroma_format_idc.getId());
        avcConfigurationBox.setConfigurationVersion(1);
        avcConfigurationBox.setLengthSizeMinusOne(3);


        avcConfigurationBox.setProfileCompatibility(
                (firstSeqParameterSet.constraint_set_0_flag ? 128 : 0) +
                        (firstSeqParameterSet.constraint_set_1_flag ? 64 : 0) +
                        (firstSeqParameterSet.constraint_set_2_flag ? 32 : 0) +
                        (firstSeqParameterSet.constraint_set_3_flag ? 16 : 0) +
                        (firstSeqParameterSet.constraint_set_4_flag ? 8 : 0) +
                        (int) (firstSeqParameterSet.reserved_zero_2bits & 0x3)
        );

        visualSampleEntry.addBox(avcConfigurationBox);
        sampleDescriptionBox.addBox(visualSampleEntry);

        trackMetaData.setCreationTime(new Date());
        trackMetaData.setModificationTime(new Date());
        trackMetaData.setLanguage(lang);
        trackMetaData.setTimescale(timescale);
        trackMetaData.setWidth(width);
        trackMetaData.setHeight(height);
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return sampleDescriptionBox;
    }




    public String getHandler() {
        return "vide";
    }

    public List<Sample> getSamples() {
        return samples;
    }

    private boolean readVariables() {
        width = (firstSeqParameterSet.pic_width_in_mbs_minus1 + 1) * 16;
        int mult = 2;
        if (firstSeqParameterSet.frame_mbs_only_flag) {
            mult = 1;
        }
        height = 16 * (firstSeqParameterSet.pic_height_in_map_units_minus1 + 1) * mult;
        if (firstSeqParameterSet.frame_cropping_flag) {
            int chromaArrayType = 0;
            if (!firstSeqParameterSet.residual_color_transform_flag) {
                chromaArrayType = firstSeqParameterSet.chroma_format_idc.getId();
            }
            int cropUnitX = 1;
            int cropUnitY = mult;
            if (chromaArrayType != 0) {
                cropUnitX = firstSeqParameterSet.chroma_format_idc.getSubWidth();
                cropUnitY = firstSeqParameterSet.chroma_format_idc.getSubHeight() * mult;
            }

            width -= cropUnitX * (firstSeqParameterSet.frame_crop_left_offset + firstSeqParameterSet.frame_crop_right_offset);
            height -= cropUnitY * (firstSeqParameterSet.frame_crop_top_offset + firstSeqParameterSet.frame_crop_bottom_offset);
        }
        return true;
    }








    private boolean readSamples(LookAhead la) throws IOException {


        List<ByteBuffer> buffered = new ArrayList<ByteBuffer>();


        ByteBuffer nal;


        class FirstVclNalDetector {

            public FirstVclNalDetector(ByteBuffer nal, int nal_ref_idc, int nal_unit_type) {
                InputStream bs = cleanBuffer(new ByteBufferBackedInputStream(nal));
                SliceHeader sh = new SliceHeader(bs, spsIdToSps, ppsIdToPps, nal_unit_type == 5);
                this.frame_num = sh.frame_num;
                this.pic_parameter_set_id = sh.pic_parameter_set_id;
                this.field_pic_flag = sh.field_pic_flag;
                this.bottom_field_flag = sh.bottom_field_flag;
                this.nal_ref_idc = nal_ref_idc;
                this.pic_order_cnt_type = spsIdToSps.get(ppsIdToPps.get(sh.pic_parameter_set_id).seq_parameter_set_id).pic_order_cnt_type;
                this.delta_pic_order_cnt_bottom = sh.delta_pic_order_cnt_bottom;
                this.pic_order_cnt_lsb = sh.pic_order_cnt_lsb;
                this.delta_pic_order_cnt_0 = sh.delta_pic_order_cnt_0;
                this.delta_pic_order_cnt_1 = sh.delta_pic_order_cnt_1;
                this.idr_pic_id = sh.idr_pic_id;
            }

            int frame_num;
            int pic_parameter_set_id;
            boolean field_pic_flag;
            boolean bottom_field_flag;
            int nal_ref_idc;
            int pic_order_cnt_type;
            int delta_pic_order_cnt_bottom;
            int pic_order_cnt_lsb;
            int delta_pic_order_cnt_0;
            int delta_pic_order_cnt_1;
            boolean idrPicFlag;
            int idr_pic_id;

            boolean isFirstInNew(FirstVclNalDetector nu) {
                if (nu.frame_num != frame_num) {
                    return true;
                }
                if (nu.pic_parameter_set_id != pic_parameter_set_id) {
                    return true;
                }
                if (nu.field_pic_flag != field_pic_flag) {
                    return true;
                }
                if (nu.field_pic_flag) {
                    if (nu.bottom_field_flag != bottom_field_flag) {
                        return true;
                    }
                }
                if (nu.nal_ref_idc != nal_ref_idc) {
                    return true;
                }
                if (nu.pic_order_cnt_type == 0 && pic_order_cnt_type == 0) {
                    if (nu.pic_order_cnt_lsb != pic_order_cnt_lsb) {
                        return true;
                    }
                    if (nu.delta_pic_order_cnt_bottom != delta_pic_order_cnt_bottom) {
                        return true;
                    }
                }
                if (nu.pic_order_cnt_type == 1 && pic_order_cnt_type == 1) {
                    if (nu.delta_pic_order_cnt_0 != delta_pic_order_cnt_0) {
                        return true;
                    }
                    if (nu.delta_pic_order_cnt_1 != delta_pic_order_cnt_1) {
                        return true;
                    }
                }
                if (nu.idrPicFlag != idrPicFlag) {
                    return true;
                }
                if (nu.idrPicFlag && idrPicFlag) {
                    if (nu.idr_pic_id != idr_pic_id) {
                        return true;
                    }
                }
                return false;
            }
        }
        FirstVclNalDetector fvnd = null;


        nal_loop:
        while ((nal = findNextNal(la)) != null) {
            int type = nal.get(0);
            int nal_ref_idc = (type >> 5) & 3;
            int nal_unit_type = type & 0x1f;



            switch (nal_unit_type) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                    FirstVclNalDetector current = new FirstVclNalDetector(nal, nal_ref_idc, nal_unit_type);
                    if (fvnd == null) {
                        fvnd = current;
                    } else if (fvnd.isFirstInNew(current)) {
                        System.err.println("Wrapping up cause of first vcl nal is found");
                        createSample(buffered);
                        fvnd = null;
                        fvnd = current;
                    }
                    buffered.add((ByteBuffer) nal.rewind());
                    System.err.println("NAL Unit Type: " + nal_unit_type + " " + fvnd.frame_num);
                    break;

                case 6:
                    if (fvnd != null) {
                        System.err.println("Wrapping up cause of SEI after vcl marks new sample");
                        createSample(buffered);
                        fvnd = null;
                    }
                    seiMessage = new SEIMessage(cleanBuffer(new ByteBufferBackedInputStream(nal)), currentSeqParameterSet);
                    buffered.add(nal);
                    break;

                case 9:
                    if (fvnd != null) {
                        System.err.println("Wrapping up cause of AU after vcl marks new sample");
                        createSample(buffered);
                        fvnd = null;
                    }
                    buffered.add(nal);
                    break;
                case 7:
                    if (fvnd != null) {
                        System.err.println("Wrapping up cause of SPS after vcl marks new sample");
                        createSample(buffered);
                        fvnd = null;
                    }
                    handleSPS((ByteBuffer) nal.rewind());
                    break;
                case 8:
                    if (fvnd != null) {
                        System.err.println("Wrapping up cause of PPS after vcl marks new sample");
                        createSample(buffered);
                        fvnd = null;
                    }
                    handlePPS((ByteBuffer) nal.rewind());
                    break;
                case 10:
                case 11:

                    break nal_loop;

                case 13:
                    throw new RuntimeException("Sequence parameter set extension is not yet handled. Needs TLC.");

                default:
                 //   System.err.println("Unknown NAL unit type: " + nal_unit_type);

            }



        }
        if (buffered.size()>0) {
            createSample(buffered);
        }
        decodingTimes = new long[samples.size()];
        Arrays.fill(decodingTimes, frametick);
        return true;
    }

    private void createSample(List<ByteBuffer> buffered) throws IOException {

        int stdpValue = 22;

        boolean IdrPicFlag = false;
        for (ByteBuffer nal : buffered) {
            int type = nal.get(0);
            int nal_unit_type = type & 0x1f;
            if (nal_unit_type == 5) {
                IdrPicFlag = true;
            }
        }

        if (IdrPicFlag) {
            stdpValue += 16;
        }
        // cleans the buffer we just added
        InputStream bs = cleanBuffer(new ByteBufferBackedInputStream(buffered.get(buffered.size() - 1)));
        SliceHeader sh = new SliceHeader(bs, spsIdToSps, ppsIdToPps, IdrPicFlag);
        if (sh.slice_type == SliceHeader.SliceType.B) {
            stdpValue += 4;
        }
        Sample bb = createSampleObject(buffered);
//                    LOG.fine("Adding sample with size " + bb.capacity() + " and header " + sh);
        buffered.clear();

        if (seiMessage == null || seiMessage.n_frames == 0) {
            frameNrInGop = 0;
        }
        int offset = 0;
        if (seiMessage != null && seiMessage.clock_timestamp_flag) {
            offset = seiMessage.n_frames - frameNrInGop;
        } else if (seiMessage != null && seiMessage.removal_delay_flag) {
            offset = seiMessage.dpb_removal_delay / 2;
        }
        ctts.add(new CompositionTimeToSample.Entry(1, offset * frametick));
        sdtp.add(new SampleDependencyTypeBox.Entry(stdpValue));
        frameNrInGop++;

        samples.add(bb);
        if (IdrPicFlag) { // IDR Picture
            stss.add(samples.size());
        }

    }





    private void handlePPS(ByteBuffer data) throws IOException {
        InputStream is = new ByteBufferBackedInputStream(data);
        is.read();

        PictureParameterSet _pictureParameterSet = PictureParameterSet.read(is);
        if (firstPictureParameterSet == null) {
            firstPictureParameterSet = _pictureParameterSet;
        }

        currentPictureParameterSet = _pictureParameterSet;

        byte[] ppsBytes = toArray((ByteBuffer) data.rewind());
        byte[] oldPpsSameId = ppsIdToPpsBytes.get(_pictureParameterSet.pic_parameter_set_id);


        if (oldPpsSameId != null && !Arrays.equals(oldPpsSameId, ppsBytes)) {
            throw new RuntimeException("OMG - I got two SPS with same ID but different settings! (AVC3 is the solution)");
        } else {
            if (oldPpsSameId == null) {
                pictureParameterRangeMap.put(samples.size(), ppsBytes);
            }
            ppsIdToPpsBytes.put(_pictureParameterSet.pic_parameter_set_id, ppsBytes);
            ppsIdToPps.put(_pictureParameterSet.pic_parameter_set_id, _pictureParameterSet);
        }


    }

    private void handleSPS(ByteBuffer data) throws IOException {
        InputStream spsInputStream = cleanBuffer(new ByteBufferBackedInputStream(data));
        spsInputStream.read();
        SeqParameterSet _seqParameterSet = SeqParameterSet.read(spsInputStream);
        if (firstSeqParameterSet == null) {
            firstSeqParameterSet = _seqParameterSet;
            configureFramerate();
        }
        currentSeqParameterSet = _seqParameterSet;

        byte[] spsBytes = toArray((ByteBuffer) data.rewind());
        byte[] oldSpsSameId = spsIdToSpsBytes.get(_seqParameterSet.seq_parameter_set_id);
        if (oldSpsSameId != null && !Arrays.equals(oldSpsSameId, spsBytes)) {
            throw new RuntimeException("OMG - I got two SPS with same ID but different settings!");
        } else {
            if (oldSpsSameId != null) {
                seqParameterRangeMap.put(samples.size(), spsBytes);
            }
            spsIdToSpsBytes.put(_seqParameterSet.seq_parameter_set_id, spsBytes);
            spsIdToSps.put(_seqParameterSet.seq_parameter_set_id, _seqParameterSet);

        }


    }


    private void configureFramerate() {
        if (determineFrameRate) {
            if (firstSeqParameterSet.vuiParams != null) {
                timescale = firstSeqParameterSet.vuiParams.time_scale >> 1; // Not sure why, but I found this in several places, and it works...
                frametick = firstSeqParameterSet.vuiParams.num_units_in_tick;
                if (timescale == 0 || frametick == 0) {
                    System.err.println("Warning: vuiParams contain invalid values: time_scale: " + timescale + " and frame_tick: " + frametick + ". Setting frame rate to 25fps");
                    timescale = 90000;
                    frametick = 3600;
                }
            } else {
                System.err.println("Warning: Can't determine frame rate. Guessing 25 fps");
                timescale = 90000;
                frametick = 3600;
            }
        }
    }

    public static class SliceHeader {

        public enum SliceType {
            P, B, I, SP, SI
        }

        public int first_mb_in_slice;
        public SliceType slice_type;
        public int pic_parameter_set_id;
        public int colour_plane_id;
        public int frame_num;
        public boolean field_pic_flag = false;
        public boolean bottom_field_flag = false;
        public int idr_pic_id;
        public int pic_order_cnt_lsb;
        public int delta_pic_order_cnt_bottom;
        public int delta_pic_order_cnt_0;
        public int delta_pic_order_cnt_1;

        public SliceHeader(InputStream is, Map<Integer, SeqParameterSet> spss, Map<Integer, PictureParameterSet> ppss, boolean IdrPicFlag) {
            try {
                is.read();
                CAVLCReader reader = new CAVLCReader(is);
                first_mb_in_slice = reader.readUE("SliceHeader: first_mb_in_slice");
                int sliceTypeInt = reader.readUE("SliceHeader: slice_type");
                switch (sliceTypeInt) {
                    case 0:
                    case 5:
                        slice_type = SliceType.P;
                        break;

                    case 1:
                    case 6:
                        slice_type = SliceType.B;
                        break;

                    case 2:
                    case 7:
                        slice_type = SliceType.I;
                        break;

                    case 3:
                    case 8:
                        slice_type = SliceType.SP;
                        break;

                    case 4:
                    case 9:
                        slice_type = SliceType.SI;
                        break;

                }

                pic_parameter_set_id = reader.readUE("SliceHeader: pic_parameter_set_id");
                PictureParameterSet pps = ppss.get(pic_parameter_set_id);
                SeqParameterSet sps = spss.get(pps.seq_parameter_set_id);
                if (sps.residual_color_transform_flag) {
                    colour_plane_id = reader.readU(2, "SliceHeader: colour_plane_id");
                }
                frame_num = reader.readU(sps.log2_max_frame_num_minus4 + 4, "SliceHeader: frame_num");

                if (!sps.frame_mbs_only_flag) {
                    field_pic_flag = reader.readBool("SliceHeader: field_pic_flag");
                    if (field_pic_flag) {
                        bottom_field_flag = reader.readBool("SliceHeader: bottom_field_flag");
                    }
                }
                if (IdrPicFlag) {

                    idr_pic_id = reader.readUE("SliceHeader: idr_pic_id");
                }
                if (sps.pic_order_cnt_type == 0) {
                    pic_order_cnt_lsb = reader.readU(sps.log2_max_pic_order_cnt_lsb_minus4 + 4, "SliceHeader: pic_order_cnt_lsb");
                    if (pps.bottom_field_pic_order_in_frame_present_flag && !field_pic_flag) {
                        delta_pic_order_cnt_bottom = reader.readSE("SliceHeader: delta_pic_order_cnt_bottom");
                    }
                }

                if (sps.pic_order_cnt_type == 1 && !sps.delta_pic_order_always_zero_flag) {

                    delta_pic_order_cnt_0 = reader.readSE("delta_pic_order_cnt_0");
                    if (pps.bottom_field_pic_order_in_frame_present_flag && !field_pic_flag) {
                        delta_pic_order_cnt_1 = reader.readSE("delta_pic_order_cnt_1");
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }

        @Override
        public String toString() {
            return "SliceHeader{" +
                    "first_mb_in_slice=" + first_mb_in_slice +
                    ", slice_type=" + slice_type +
                    ", pic_parameter_set_id=" + pic_parameter_set_id +
                    ", colour_plane_id=" + colour_plane_id +
                    ", frame_num=" + frame_num +
                    ", field_pic_flag=" + field_pic_flag +
                    ", bottom_field_flag=" + bottom_field_flag +
                    ", idr_pic_id=" + idr_pic_id +
                    ", pic_order_cnt_lsb=" + pic_order_cnt_lsb +
                    ", delta_pic_order_cnt_bottom=" + delta_pic_order_cnt_bottom +
                    '}';
        }
    }

    public class ByteBufferBackedInputStream extends InputStream {

        private final ByteBuffer buf;

        public ByteBufferBackedInputStream(ByteBuffer buf) {
            // make a coy of the buffer
            this.buf = buf.duplicate();
        }

        public int read() throws IOException {
            if (!buf.hasRemaining()) {
                return -1;
            }
            return buf.get() & 0xFF;
        }

        public int read(byte[] bytes, int off, int len)
                throws IOException {
            if (!buf.hasRemaining()) {
                return -1;
            }

            len = Math.min(len, buf.remaining());
            buf.get(bytes, off, len);
            return len;
        }
    }

    public class SEIMessage {

        int payloadType = 0;
        int payloadSize = 0;

        boolean removal_delay_flag;
        int cpb_removal_delay;
        int dpb_removal_delay;

        boolean clock_timestamp_flag;
        int pic_struct;
        int ct_type;
        int nuit_field_based_flag;
        int counting_type;
        int full_timestamp_flag;
        int discontinuity_flag;
        int cnt_dropped_flag;
        int n_frames;
        int seconds_value;
        int minutes_value;
        int hours_value;
        int time_offset_length;
        int time_offset;

        SeqParameterSet sps;

        public SEIMessage(InputStream is, SeqParameterSet sps) throws IOException {
            this.sps = sps;
            is.read();
            int datasize = is.available();
            int read = 0;
            while (read < datasize) {
                payloadType = 0;
                payloadSize = 0;
                int last_payload_type_bytes = is.read();
                read++;
                while (last_payload_type_bytes == 0xff) {
                    payloadType += last_payload_type_bytes;
                    last_payload_type_bytes = is.read();
                    read++;
                }
                payloadType += last_payload_type_bytes;
                int last_payload_size_bytes = is.read();
                read++;

                while (last_payload_size_bytes == 0xff) {
                    payloadSize += last_payload_size_bytes;
                    last_payload_size_bytes = is.read();
                    read++;
                }
                payloadSize += last_payload_size_bytes;
                if (datasize - read >= payloadSize) {
                    if (payloadType == 1) { // pic_timing is what we are interested in!
                        if (sps.vuiParams != null && (sps.vuiParams.nalHRDParams != null || sps.vuiParams.vclHRDParams != null || sps.vuiParams.pic_struct_present_flag)) {
                            byte[] data = new byte[payloadSize];
                            is.read(data);
                            read += payloadSize;
                            CAVLCReader reader = new CAVLCReader(new ByteArrayInputStream(data));
                            if (sps.vuiParams.nalHRDParams != null || sps.vuiParams.vclHRDParams != null) {
                                removal_delay_flag = true;
                                cpb_removal_delay = reader.readU(sps.vuiParams.nalHRDParams.cpb_removal_delay_length_minus1 + 1, "SEI: cpb_removal_delay");
                                dpb_removal_delay = reader.readU(sps.vuiParams.nalHRDParams.dpb_output_delay_length_minus1 + 1, "SEI: dpb_removal_delay");
                            } else {
                                removal_delay_flag = false;
                            }
                            if (sps.vuiParams.pic_struct_present_flag) {
                                pic_struct = reader.readU(4, "SEI: pic_struct");
                                int numClockTS;
                                switch (pic_struct) {
                                    case 0:
                                    case 1:
                                    case 2:
                                    default:
                                        numClockTS = 1;
                                        break;

                                    case 3:
                                    case 4:
                                    case 7:
                                        numClockTS = 2;
                                        break;

                                    case 5:
                                    case 6:
                                    case 8:
                                        numClockTS = 3;
                                        break;
                                }
                                for (int i = 0; i < numClockTS; i++) {
                                    clock_timestamp_flag = reader.readBool("pic_timing SEI: clock_timestamp_flag[" + i + "]");
                                    if (clock_timestamp_flag) {
                                        ct_type = reader.readU(2, "pic_timing SEI: ct_type");
                                        nuit_field_based_flag = reader.readU(1, "pic_timing SEI: nuit_field_based_flag");
                                        counting_type = reader.readU(5, "pic_timing SEI: counting_type");
                                        full_timestamp_flag = reader.readU(1, "pic_timing SEI: full_timestamp_flag");
                                        discontinuity_flag = reader.readU(1, "pic_timing SEI: discontinuity_flag");
                                        cnt_dropped_flag = reader.readU(1, "pic_timing SEI: cnt_dropped_flag");
                                        n_frames = reader.readU(8, "pic_timing SEI: n_frames");
                                        if (full_timestamp_flag == 1) {
                                            seconds_value = reader.readU(6, "pic_timing SEI: seconds_value");
                                            minutes_value = reader.readU(6, "pic_timing SEI: minutes_value");
                                            hours_value = reader.readU(5, "pic_timing SEI: hours_value");
                                        } else {
                                            if (reader.readBool("pic_timing SEI: seconds_flag")) {
                                                seconds_value = reader.readU(6, "pic_timing SEI: seconds_value");
                                                if (reader.readBool("pic_timing SEI: minutes_flag")) {
                                                    minutes_value = reader.readU(6, "pic_timing SEI: minutes_value");
                                                    if (reader.readBool("pic_timing SEI: hours_flag")) {
                                                        hours_value = reader.readU(5, "pic_timing SEI: hours_value");
                                                    }
                                                }
                                            }
                                        }
                                        if (true) {
                                            if (sps.vuiParams.nalHRDParams != null) {
                                                time_offset_length = sps.vuiParams.nalHRDParams.time_offset_length;
                                            } else if (sps.vuiParams.vclHRDParams != null) {
                                                time_offset_length = sps.vuiParams.vclHRDParams.time_offset_length;
                                            } else {
                                                time_offset_length = 24;
                                            }
                                            time_offset = reader.readU(24, "pic_timing SEI: time_offset");
                                        }
                                    }
                                }
                            }

                        } else {
                            for (int i = 0; i < payloadSize; i++) {
                                is.read();
                                read++;
                            }
                        }
                    } else {
                        for (int i = 0; i < payloadSize; i++) {
                            is.read();
                            read++;
                        }
                    }
                } else {
                    read = datasize;
                }
                LOG.fine(this.toString());
            }
        }

        @Override
        public String toString() {
            String out = "SEIMessage{" +
                    "payloadType=" + payloadType +
                    ", payloadSize=" + payloadSize;
            if (payloadType == 1) {
                if (sps.vuiParams.nalHRDParams != null || sps.vuiParams.vclHRDParams != null) {

                    out += ", cpb_removal_delay=" + cpb_removal_delay +
                            ", dpb_removal_delay=" + dpb_removal_delay;
                }
                if (sps.vuiParams.pic_struct_present_flag) {
                    out += ", pic_struct=" + pic_struct;
                    if (clock_timestamp_flag) {
                        out += ", ct_type=" + ct_type +
                                ", nuit_field_based_flag=" + nuit_field_based_flag +
                                ", counting_type=" + counting_type +
                                ", full_timestamp_flag=" + full_timestamp_flag +
                                ", discontinuity_flag=" + discontinuity_flag +
                                ", cnt_dropped_flag=" + cnt_dropped_flag +
                                ", n_frames=" + n_frames +
                                ", seconds_value=" + seconds_value +
                                ", minutes_value=" + minutes_value +
                                ", hours_value=" + hours_value +
                                ", time_offset_length=" + time_offset_length +
                                ", time_offset=" + time_offset;
                    }
                }
            }
            out += '}';
            return out;
        }
    }
}
