package org.mp4parser.streaming.input.h264;

import org.mp4parser.boxes.iso14496.part12.SampleDescriptionBox;
import org.mp4parser.boxes.iso14496.part15.AvcConfigurationBox;
import org.mp4parser.boxes.sampleentry.VisualSampleEntry;
import org.mp4parser.streaming.StreamingSample;
import org.mp4parser.streaming.extensions.CompositionTimeSampleExtension;
import org.mp4parser.streaming.extensions.CompositionTimeTrackExtension;
import org.mp4parser.streaming.extensions.DimensionTrackExtension;
import org.mp4parser.streaming.extensions.SampleFlagsSampleExtension;
import org.mp4parser.streaming.input.StreamingSampleImpl;
import org.mp4parser.streaming.input.h264.spspps.PictureParameterSet;
import org.mp4parser.streaming.input.h264.spspps.SeqParameterSet;
import org.mp4parser.streaming.input.h264.spspps.SliceHeader;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;


public abstract class H264NalConsumingTrack extends AbstractH264Track {
    private static final Logger LOG = Logger.getLogger(H264NalConsumingTrack.class.getName());
    int max_dec_frame_buffering = 16;
    List<StreamingSample> decFrameBuffer = new ArrayList<StreamingSample>();
    List<StreamingSample> decFrameBuffer2 = new ArrayList<StreamingSample>();
    LinkedHashMap<Integer, ByteBuffer> spsIdToSpsBytes = new LinkedHashMap<Integer, ByteBuffer>();
    LinkedHashMap<Integer, SeqParameterSet> spsIdToSps = new LinkedHashMap<Integer, SeqParameterSet>();
    LinkedHashMap<Integer, ByteBuffer> ppsIdToPpsBytes = new LinkedHashMap<Integer, ByteBuffer>();
    LinkedHashMap<Integer, PictureParameterSet> ppsIdToPps = new LinkedHashMap<Integer, PictureParameterSet>();
    BlockingQueue<SeqParameterSet> spsForConfig = new LinkedBlockingDeque<SeqParameterSet>();

    int timescale = 0;
    int frametick = 0;
    boolean configured;

    SampleDescriptionBox stsd;
    SeqParameterSet currentSeqParameterSet = null;
    PictureParameterSet currentPictureParameterSet = null;
    List<ByteBuffer> buffered = new ArrayList<ByteBuffer>();
    FirstVclNalDetector fvnd = null;
    H264NalUnitHeader sliceNalUnitHeader;

    public H264NalConsumingTrack() {
    }

    public static H264NalUnitHeader getNalUnitHeader(ByteBuffer nal) {
        H264NalUnitHeader nalUnitHeader = new H264NalUnitHeader();
        int type = nal.get(0);
        nalUnitHeader.nal_ref_idc = (type >> 5) & 3;
        nalUnitHeader.nal_unit_type = type & 0x1f;

        return nalUnitHeader;
    }

    protected void consumeNal(ByteBuffer nal) throws IOException {
        //LOG.finest("Consume NAL of " + nal.length + " bytes." + Hex.encodeHex(new byte[]{nal[0], nal[1], nal[2], nal[3], nal[4]}));
        H264NalUnitHeader nalUnitHeader = getNalUnitHeader(nal);
        switch (nalUnitHeader.nal_unit_type) {
            case H264NalUnitTypes.CODED_SLICE_NON_IDR:
            case H264NalUnitTypes.CODED_SLICE_DATA_PART_A:
            case H264NalUnitTypes.CODED_SLICE_DATA_PART_B:
            case H264NalUnitTypes.CODED_SLICE_DATA_PART_C:
            case H264NalUnitTypes.CODED_SLICE_IDR:
                FirstVclNalDetector current = new FirstVclNalDetector(nal,
                        nalUnitHeader.nal_ref_idc, nalUnitHeader.nal_unit_type);
                sliceNalUnitHeader = nalUnitHeader;
                if (fvnd != null && fvnd.isFirstInNew(current)) {
                    LOG.finer("Wrapping up cause of first vcl nal is found");
                    pushSample(createSample(buffered, fvnd.sliceHeader, sliceNalUnitHeader), false, false);
                    buffered.clear();
                }
                fvnd = current;
                //System.err.println("" + nalUnitHeader.nal_unit_type);
                buffered.add(nal);
                //log.finer("NAL Unit Type: " + nalUnitHeader.nal_unit_type + " " + fvnd.frame_num);
                break;

            case H264NalUnitTypes.SEI:
                if (fvnd != null) {
                    LOG.finer("Wrapping up cause of SEI after vcl marks new sample");
                    pushSample(createSample(buffered, fvnd.sliceHeader, sliceNalUnitHeader), false, false);
                    buffered.clear();
                    fvnd = null;
                }
                //System.err.println("" + nalUnitHeader.nal_unit_type);
                buffered.add(nal);
                break;

            case H264NalUnitTypes.AU_UNIT_DELIMITER:
                if (fvnd != null) {
                    LOG.finer("Wrapping up cause of AU after vcl marks new sample");
                    pushSample(createSample(buffered, fvnd.sliceHeader, sliceNalUnitHeader), false, false);
                    buffered.clear();
                    fvnd = null;
                }
                //System.err.println("" + nalUnitHeader.nal_unit_type);
                buffered.add(nal);
                break;
            case H264NalUnitTypes.SEQ_PARAMETER_SET:
                if (fvnd != null) {
                    LOG.finer("Wrapping up cause of SPS after vcl marks new sample");
                    pushSample(createSample(buffered, fvnd.sliceHeader, sliceNalUnitHeader), false, false);
                    buffered.clear();
                    fvnd = null;
                }
                handleSPS(nal);
                break;
            case 8:
                if (fvnd != null) {
                    LOG.finer("Wrapping up cause of PPS after vcl marks new sample");
                    pushSample(createSample(buffered, fvnd.sliceHeader, sliceNalUnitHeader), false, false);
                    buffered.clear();
                    fvnd = null;
                }
                handlePPS(nal);
                break;
            case H264NalUnitTypes.END_OF_SEQUENCE:
            case H264NalUnitTypes.END_OF_STREAM:

                return;

            case H264NalUnitTypes.SEQ_PARAMETER_SET_EXT:
                throw new IOException("Sequence parameter set extension is not yet handled. Needs TLC.");

            default:
                //  buffered.add(nal);
                LOG.warning("Unknown NAL unit type: " + nalUnitHeader.nal_unit_type);

        }


    }

    protected void pushSample(StreamingSample ss, boolean all, boolean force) throws IOException {
        if (ss != null) {
            decFrameBuffer.add(ss);
        }
        if (all) {
            while (decFrameBuffer.size() > 0) {
                pushSample(null, false, true);
            }
        } else {
            if ((decFrameBuffer.size() - 1 > max_dec_frame_buffering) || force) {
                StreamingSample first = decFrameBuffer.remove(0);
                PictureOrderCountType0SampleExtension poct0se = first.getSampleExtension(PictureOrderCountType0SampleExtension.class);
                if (poct0se == null) {
                    sampleSink.acceptSample(first, this);
                } else {
                    int delay = 0;
                    for (StreamingSample streamingSample : decFrameBuffer) {
                        if (poct0se.getPoc() > streamingSample.getSampleExtension(PictureOrderCountType0SampleExtension.class).getPoc()) {
                            delay++;
                        }
                    }
                    for (StreamingSample streamingSample : decFrameBuffer2) {
                        if (poct0se.getPoc() < streamingSample.getSampleExtension(PictureOrderCountType0SampleExtension.class).getPoc()) {
                            delay--;
                        }
                    }
                    decFrameBuffer2.add(first);
                    if (decFrameBuffer2.size() > max_dec_frame_buffering) {
                        decFrameBuffer2.remove(0).removeSampleExtension(PictureOrderCountType0SampleExtension.class);
                    }

                    first.addSampleExtension(CompositionTimeSampleExtension.create(delay * frametick));
                    //System.err.println("Adding sample");
                    sampleSink.acceptSample(first, this);
                }
            }
        }

    }


    protected SampleFlagsSampleExtension createSampleFlagsSampleExtension(H264NalUnitHeader nu, SliceHeader sliceHeader) {
        SampleFlagsSampleExtension sampleFlagsSampleExtension = new SampleFlagsSampleExtension();
        if (nu.nal_ref_idc == 0) {
            sampleFlagsSampleExtension.setSampleIsDependedOn(2);
        } else {
            sampleFlagsSampleExtension.setSampleIsDependedOn(1);
        }
        if ((sliceHeader.slice_type == SliceHeader.SliceType.I) || (sliceHeader.slice_type == SliceHeader.SliceType.SI)) {
            sampleFlagsSampleExtension.setSampleDependsOn(2);
        } else {
            sampleFlagsSampleExtension.setSampleDependsOn(1);
        }
        sampleFlagsSampleExtension.setSampleIsNonSyncSample(H264NalUnitTypes.CODED_SLICE_IDR != nu.nal_unit_type);
        return sampleFlagsSampleExtension;
    }

    protected PictureOrderCountType0SampleExtension createPictureOrderCountType0SampleExtension(SliceHeader sliceHeader) {
        if (sliceHeader.sps.pic_order_cnt_type == 0) {
            return new PictureOrderCountType0SampleExtension(
                    sliceHeader, decFrameBuffer.size() > 0 ?
                    decFrameBuffer.get(decFrameBuffer.size() - 1).getSampleExtension(PictureOrderCountType0SampleExtension.class) :
                    null);
/*            decFrameBuffer.add(ssi);
            if (decFrameBuffer.size() - 1 > max_dec_frame_buffering) { // just added one
                drainDecPictureBuffer(false);
            }*/
        } else if (sliceHeader.sps.pic_order_cnt_type == 1) {
            throw new RuntimeException("pic_order_cnt_type == 1 needs to be implemented");
        } else if (sliceHeader.sps.pic_order_cnt_type == 2) {
            return null; // no ctts
        }
        throw new RuntimeException("I don't know sliceHeader.sps.pic_order_cnt_type of " + sliceHeader.sps.pic_order_cnt_type);
    }


    protected StreamingSample createSample(List<ByteBuffer> nals, SliceHeader sliceHeader, H264NalUnitHeader nu) throws IOException {
        LOG.finer("Create Sample");
        configure();
        if (timescale == 0 || frametick == 0) {
            throw new IOException("Frame Rate needs to be configured either by hand or by SPS before samples can be created");
        }


        StreamingSample ss = new StreamingSampleImpl(
                nals,
                frametick);
        ss.addSampleExtension(createSampleFlagsSampleExtension(nu, sliceHeader));
        ss.addSampleExtension(createPictureOrderCountType0SampleExtension(sliceHeader));

        return ss;
    }


    public void setFrametick(int frametick) {
        this.frametick = frametick;
    }

    public synchronized void configure() {

        if (!configured) {
            SeqParameterSet sps;
            try {
                sps = spsForConfig.poll(5L, TimeUnit.SECONDS);
                if (sps == null) {
                    LOG.warning("Can't determine frame rate as no SPS became available in time");
                    return;
                }
            } catch (InterruptedException e) {
                LOG.warning(e.getMessage());
                LOG.warning("Can't determine frame rate as no SPS became available in time");
                return;
            }

            if (sps.pic_order_cnt_type == 0 || sps.pic_order_cnt_type == 1) {
                this.addTrackExtension(new CompositionTimeTrackExtension());
            }

            int width = (sps.pic_width_in_mbs_minus1 + 1) * 16;
            int mult = 2;
            if (sps.frame_mbs_only_flag) {
                mult = 1;
            }
            int height = 16 * (sps.pic_height_in_map_units_minus1 + 1) * mult;
            if (sps.frame_cropping_flag) {
                int chromaArrayType = 0;
                if (!sps.residual_color_transform_flag) {
                    chromaArrayType = sps.chroma_format_idc.getId();
                }
                int cropUnitX = 1;
                int cropUnitY = mult;
                if (chromaArrayType != 0) {
                    cropUnitX = sps.chroma_format_idc.getSubWidth();
                    cropUnitY = sps.chroma_format_idc.getSubHeight() * mult;
                }

                width -= cropUnitX * (sps.frame_crop_left_offset + sps.frame_crop_right_offset);
                height -= cropUnitY * (sps.frame_crop_top_offset + sps.frame_crop_bottom_offset);
            }


            VisualSampleEntry visualSampleEntry = new VisualSampleEntry("avc1");
            visualSampleEntry.setDataReferenceIndex(1);
            visualSampleEntry.setDepth(24);
            visualSampleEntry.setFrameCount(1);
            visualSampleEntry.setHorizresolution(72);
            visualSampleEntry.setVertresolution(72);
            DimensionTrackExtension dte = this.getTrackExtension(DimensionTrackExtension.class);
            if (dte == null) {
                this.addTrackExtension(new DimensionTrackExtension(width, height));
            }
            visualSampleEntry.setWidth(width);
            visualSampleEntry.setHeight(height);

            visualSampleEntry.setCompressorname("AVC Coding");

            AvcConfigurationBox avcConfigurationBox = new AvcConfigurationBox();

            avcConfigurationBox.setSequenceParameterSets(new ArrayList<ByteBuffer>(spsIdToSpsBytes.values()));
            avcConfigurationBox.setPictureParameterSets(new ArrayList<ByteBuffer>(ppsIdToPpsBytes.values()));
            avcConfigurationBox.setAvcLevelIndication(sps.level_idc);
            avcConfigurationBox.setAvcProfileIndication(sps.profile_idc);
            avcConfigurationBox.setBitDepthLumaMinus8(sps.bit_depth_luma_minus8);
            avcConfigurationBox.setBitDepthChromaMinus8(sps.bit_depth_chroma_minus8);
            avcConfigurationBox.setChromaFormat(sps.chroma_format_idc.getId());
            avcConfigurationBox.setConfigurationVersion(1);
            avcConfigurationBox.setLengthSizeMinusOne(3);


            avcConfigurationBox.setProfileCompatibility(
                    (sps.constraint_set_0_flag ? 128 : 0) +
                            (sps.constraint_set_1_flag ? 64 : 0) +
                            (sps.constraint_set_2_flag ? 32 : 0) +
                            (sps.constraint_set_3_flag ? 16 : 0) +
                            (sps.constraint_set_4_flag ? 8 : 0) +
                            (int) (sps.reserved_zero_2bits & 0x3)
            );

            visualSampleEntry.addBox(avcConfigurationBox);
            stsd = new SampleDescriptionBox();
            stsd.addBox(visualSampleEntry);

            int _timescale;
            int _frametick;
            if (sps.vuiParams != null) {
                _timescale = sps.vuiParams.time_scale >> 1; // Not sure why, but I found this in several places, and it works...
                _frametick = sps.vuiParams.num_units_in_tick;
                if (_timescale == 0 || _frametick == 0) {
                    LOG.warning("vuiParams contain invalid values: time_scale: " + _timescale + " and frame_tick: " + _frametick + ". Setting frame rate to 25fps");
                    _timescale = 0;
                    _frametick = 0;
                }
                if (_frametick > 0) {
                    if (_timescale / _frametick > 100) {
                        LOG.warning("Framerate is " + (_timescale / _frametick) + ". That is suspicious.");
                    }
                } else {
                    LOG.warning("Frametick is " + _frametick + ". That is suspicious.");
                }
                if (sps.vuiParams.bitstreamRestriction != null) {
                    max_dec_frame_buffering = sps.vuiParams.bitstreamRestriction.max_dec_frame_buffering;
                }
            } else {
                LOG.warning("Can't determine frame rate as SPS does not contain vuiParama");
                _timescale = 0;
                _frametick = 0;
            }
            if (timescale == 0) {
                timescale = _timescale;
            }
            if (frametick == 0) {
                frametick = _frametick;
            }
            if (sps.pic_order_cnt_type == 0) {
                this.addTrackExtension(new CompositionTimeTrackExtension());
            } else if (sps.pic_order_cnt_type == 1) {
                throw new RuntimeException("Have not yet imlemented pic_order_cnt_type 1");
            }
            configured = true;
        }
    }

    public long getTimescale() {
        configure();
        return timescale;
    }

    public void setTimescale(int timescale) {
        this.timescale = timescale;
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        configure();
        return stsd;
    }


    public String getHandler() {
        return "vide";
    }

    public String getLanguage() {
        return "eng";
    }

    protected void handlePPS(ByteBuffer nal) {
        nal.position(1);
        PictureParameterSet _pictureParameterSet = null;
        try {
            _pictureParameterSet = PictureParameterSet.read(nal);
            currentPictureParameterSet = _pictureParameterSet;


            ByteBuffer oldPpsSameId = ppsIdToPpsBytes.get(_pictureParameterSet.pic_parameter_set_id);


            if (oldPpsSameId != null && !oldPpsSameId.equals(nal)) {
                throw new RuntimeException("OMG - I got two SPS with same ID but different settings! (AVC3 is the solution)");
            } else {
                ppsIdToPpsBytes.put(_pictureParameterSet.pic_parameter_set_id, nal);
                ppsIdToPps.put(_pictureParameterSet.pic_parameter_set_id, _pictureParameterSet);
            }
        } catch (IOException e) {
            throw new RuntimeException("That's surprising to get IOException when working on ByteArrayInputStream", e);
        }


    }

    protected void handleSPS(ByteBuffer data) {
        data.position(1);
        try {
            SeqParameterSet _seqParameterSet = SeqParameterSet.read(data);

            currentSeqParameterSet = _seqParameterSet;

            ByteBuffer oldSpsSameId = spsIdToSpsBytes.get(_seqParameterSet.seq_parameter_set_id);
            if (oldSpsSameId != null && !oldSpsSameId.equals(data)) {
                throw new RuntimeException("OMG - I got two SPS with same ID but different settings!");
            } else {
                spsIdToSpsBytes.put(_seqParameterSet.seq_parameter_set_id, data);
                spsIdToSps.put(_seqParameterSet.seq_parameter_set_id, _seqParameterSet);
                spsForConfig.add(_seqParameterSet);
            }
        } catch (IOException e) {
            throw new RuntimeException("That's surprising to get IOException when working on ByteArrayInputStream", e);
        }

    }

    public void close() throws IOException {

    }

    class FirstVclNalDetector {

        public final SliceHeader sliceHeader;
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

        public FirstVclNalDetector(ByteBuffer nal, int nal_ref_idc, int nal_unit_type) {

            SliceHeader sh = new SliceHeader(nal, spsIdToSps, ppsIdToPps, nal_unit_type == 5);
            this.sliceHeader = sh;
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
}
