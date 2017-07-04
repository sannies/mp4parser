package org.mp4parser.muxer.tracks.h263;

import org.mp4parser.boxes.iso14496.part1.objectdescriptors.*;
import org.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.sampleentry.VisualSampleEntry;
import org.mp4parser.muxer.DataSource;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.SampleImpl;
import org.mp4parser.muxer.tracks.AbstractH26XTrack;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.Mp4Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mp4parser.tools.CastUtils.l2i;

/**
 *
 */
public class H263TrackImpl extends AbstractH26XTrack {
    private static Logger LOG = LoggerFactory.getLogger(ESDescriptor.class.getName());

    private int RECTANGULAR = 0;
    private int BINARY = 1;
    private int BINARY_ONLY = 2;
    private int GRAYSCALE = 3;

    private List<Sample> samples = new ArrayList<>();

    private List<ByteBuffer> esdsStuff = new ArrayList<ByteBuffer>();
    private boolean esdsComplete = false;
    private int fixed_vop_time_increment = -1;
    private int vop_time_increment_resolution = 0;
    private VisualSampleEntry mp4v;

    public H263TrackImpl(DataSource dataSource) throws IOException {
        super(dataSource, false);
        LookAhead la = new LookAhead(dataSource);
        ByteBuffer nal;
        List<ByteBuffer> nalsInSample = new ArrayList<ByteBuffer>();
        int visual_object_verid = 0;

        mp4v = new VisualSampleEntry("mp4v");

        long last_sync_point = 0;
        long last_time_code = -1;

        while ((nal = findNextNal(la)) != null) {
            ByteBuffer origNal = nal.duplicate();
            int type = IsoTypeReader.readUInt8(nal);
            if (((type == 0xb0) || // visual_object_sequence_start_code
                    (type == 0xb5) || // visual_object_start_code
                    (type == 0x00) || // video_object_start_code
                    (type == 0x20) || // video_object_layer_start_code
                    (type == 0xb2)) // user_data_start_code
                    ) {
                if (!esdsComplete) {
                    esdsStuff.add(origNal);

                    if (type == 0x20) {
                        parse0x20Unit(nal, visual_object_verid, mp4v);
                    } else if (type == 0xb5) {
                        visual_object_verid = parse0x05Unit(nal);
                    }
                }
            } else if (type == 0xb3) { // group_of_vop_start_code
                esdsComplete = true;
                BitReaderBuffer bitReaderBuffer = new BitReaderBuffer(nal);
                int time_code = bitReaderBuffer.readBits(18);
                last_sync_point = ((time_code & 0x3f) + ((time_code >>> 7 & 0x3f) * 60) + ((time_code >>> 13 & 0x1f) * 60 * 60));
                stss.add(samples.size() + 1);
                nalsInSample.add(origNal);
            } else if (type == 0xb6) { // vop_start_code
                BitReaderBuffer brb = new BitReaderBuffer(nal);
                int vop_coding_type = brb.readBits(2);
                while (brb.readBool()) {
                    last_sync_point++;
                }
                boolean marker_bit = brb.readBool();
                int i = 0;
                while (vop_time_increment_resolution >= (1 << i)) {
                    i++;
                }
                int vop_time_increment = brb.readBits(i);
                long time_code = (last_sync_point * vop_time_increment_resolution + (vop_time_increment % vop_time_increment_resolution));
                if (last_time_code != -1) {
                    decodingTimes = Mp4Arrays.copyOfAndAppend(decodingTimes, new long[]{time_code - last_time_code});
                }
                System.err.println("Frame increment: " + (time_code - last_time_code) + " vop time increment: " + vop_time_increment + " last_sync_point: " + last_sync_point + " time_code: " + time_code);
                last_time_code = time_code;
                nalsInSample.add(origNal);
                samples.add(createSampleObject(nalsInSample));
                nalsInSample.clear();
            } else {
                throw new RuntimeException("Got start code I don't know. Ask Sebastian via mp4parser mailing list what to do");
            }


        }
        // I cannot know the decoding time of the last sample therefore I'll just assume it's as long on the screen as
        // the sample before. I must have lots fantasy to imagine an edge that will make it noticeable.
        decodingTimes = Mp4Arrays.copyOfAndAppend(decodingTimes, new long[]{decodingTimes[decodingTimes.length - 1]});

        ESDescriptor esDescriptor = new ESDescriptor();
        esDescriptor.setEsId(1);
        DecoderConfigDescriptor decoderConfigDescriptor = new DecoderConfigDescriptor();
        decoderConfigDescriptor.setObjectTypeIndication(32);
        decoderConfigDescriptor.setStreamType(4);
        DecoderSpecificInfo decoderSpecificInfo = new DecoderSpecificInfo();
        Sample s = createSampleObject(esdsStuff);
        byte[] data = new byte[l2i(s.getSize())];
        s.asByteBuffer().get(data);
        decoderSpecificInfo.setData(data);
        decoderConfigDescriptor.setDecoderSpecificInfo(decoderSpecificInfo);
        esDescriptor.setDecoderConfigDescriptor(decoderConfigDescriptor);
        SLConfigDescriptor slConfigDescriptor = new SLConfigDescriptor();
        slConfigDescriptor.setPredefined(2);
        esDescriptor.setSlConfigDescriptor(slConfigDescriptor);

        ESDescriptorBox esds = new ESDescriptorBox();
        esds.setEsDescriptor(esDescriptor);
        mp4v.addBox(esds);

        trackMetaData.setTimescale(vop_time_increment_resolution);

    }

    @Override
    protected SampleEntry getCurrentSampleEntry() {
        return null;
    }


    private int parse0x05Unit(ByteBuffer nal) {
        int visual_object_verid = 0;
        BitReaderBuffer brb = new BitReaderBuffer(nal);
        boolean is_visual_object_identifier = brb.readBool();
        if (is_visual_object_identifier) {
            visual_object_verid = brb.readBits(4);
            int visual_object_priority = brb.readBits(3);
        }
        return visual_object_verid;
    }

    private void parse0x20Unit(ByteBuffer nal, int visual_object_verid, VisualSampleEntry mp4v) {
        BitReaderBuffer brb = new BitReaderBuffer(nal);
        boolean random_accessible_vol = brb.readBool();
        int video_object_type_indication = brb.readBits(8);
        boolean is_object_layer_identifier = brb.readBool();
        int video_object_layer_verid = visual_object_verid;
        if (is_object_layer_identifier) {
            video_object_layer_verid = brb.readBits(4);
            int video_object_layer_priority = brb.readBits(3);
        }
        int aspect_ratio_info = brb.readBits(4);
        // 0000  Forbidden
        // 0001 1:1 (Square)
        // 0010  12:11 (625-type for 4:3 picture)
        // 0011  10:11 (525-type for 4:3 picture)
        // 0100  16:11 (625-type stretched for 16:9 picture)
        // 0101  40:33 (525-type stretched for 16:9 picture)
        // 0110-1110 Reserved
        // 1111 extended PAR
        if (aspect_ratio_info == 15) {
            int par_width = brb.readBits(8);
            int par_height = brb.readBits(8);
        }
        boolean vol_control_parameters = brb.readBool();
        if (vol_control_parameters) {
            int chroma_format = brb.readBits(2);
            boolean low_delay = brb.readBool();
            boolean vbv_parameters = brb.readBool();
            if (vbv_parameters) {
                throw new RuntimeException("Implemented when needed");
                // first_half_bit_rate  15 uimsbf
                // marker_bit  1 bslbf
                // latter_half_bit_rate  15 uimsbf
                // marker_bit  1 bslbf
                // first_half_vbv_buffer_size  15 uimsbf
                // marker_bit  1 bslbf
                // latter_half_vbv_buffer_size  3 uimsbf
                // first_half_vbv_occupancy  11 uimsbf
                // marker_bit  1 blsbf
                // latter_half_vbv_occupancy  15 uimsbf
                // marker_bit  1 blsbf
            }

        }
        int video_object_layer_shape = brb.readBits(2);
        // 00 rectangular
        // 01 binary
        // 10 binary only
        // 11 grayscale


        if (video_object_layer_shape == GRAYSCALE
                && video_object_layer_verid != 1) {
            int video_object_layer_shape_extension = brb.readBits(4);
        }
        boolean marker_bit = brb.readBool();
        vop_time_increment_resolution = brb.readBits(16);
        marker_bit = brb.readBool();
        boolean fixed_vop_rate = brb.readBool();
        if (fixed_vop_rate) {
            LOG.info("Fixed Frame Rate");
            int i = 0;
            while (vop_time_increment_resolution >= (1 << i)) {
                i++;
            }
            fixed_vop_time_increment = brb.readBits(i);
        }
        if (video_object_layer_shape != BINARY_ONLY) {
            if (video_object_layer_shape == RECTANGULAR) {
                brb.readBool();
                int video_object_layer_width = brb.readBits(13);
                mp4v.setWidth(video_object_layer_width);
                brb.readBool();
                int video_object_layer_height = brb.readBits(13);
                mp4v.setHeight(video_object_layer_height);
                brb.readBool();
            }
/****************************************************************************************************************************
 *
 * SOMETHING IS NOT RIGHT WITH THE FOLLOWING CODE TO PARSE THE  video_object_layer BUT I DON'T NEED IT SO
 * I WON'T DEBUG RIGHT NOW. The data is copied into the MP4 byte by byte so it's really not an issue
 *
 ***************************************************************************************************************************/

          /*  boolean interlaced = brb.readBool();
            boolean obmc_disable = brb.readBool();
            int sprite_enable;
            // 0  00  sprite not used
            // 1  01 static (Basic/Low Latency)
            // -  10  GMC (Global Motion Compensation)
            // -  11 Reserved
            if (video_object_layer_verid == 1) {
                sprite_enable = brb.readBits(1);
            } else {
                sprite_enable = brb.readBits(2);
            }
            if (sprite_enable == 1 || sprite_enable == 2) {
                if (sprite_enable != 2) {
                    int sprite_width = brb.readBits(13);
                    marker_bit = brb.readBool();
                    int sprite_height = brb.readBits(13);
                    marker_bit = brb.readBool();
                    int sprite_left_coordinate = brb.readBits(13);
                    marker_bit = brb.readBool();

                    int sprite_top_coordinate = brb.readBits(13);
                    marker_bit = brb.readBool();
                }

                int no_of_sprite_warping_points = brb.readBits(6);
                int sprite_warping_accuracy = brb.readBits(2);
                boolean sprite_brightness_change = brb.readBool();
                if (sprite_enable != 2) {
                    boolean low_latency_sprite_enable = brb.readBool();
                }
            }


            if (video_object_layer_verid != 1 &&
                    video_object_layer_shape != RECTANGULAR) {
                boolean sadct_disable = brb.readBool();
            }
            boolean not_8_bit = brb.readBool();
            if (not_8_bit) {
                int quant_precision = brb.readBits(4);
                int bits_per_pixel = brb.readBits(4);
            }
            if (video_object_layer_shape == GRAYSCALE) {
                boolean no_gray_quant_update = brb.readBool();
                boolean composition_method = brb.readBool();
                boolean linear_composition = brb.readBool();
            }

            boolean quant_type = brb.readBool();
            if (quant_type) {
                boolean load_intra_quant_mat = brb.readBool();
                if (load_intra_quant_mat) {
                    throw new RuntimeException("Please implement me");
                    // intra_quant_mat 8 *[2 - 64]uimsbf
                }
                boolean load_nonintra_quant_mat = brb.readBool();
                if (load_nonintra_quant_mat) {
                    throw new RuntimeException("Please implement me");
                    // nonintra_quant_mat 8 *[2 - 64]uimsbf
                }
                if (video_object_layer_shape == GRAYSCALE) {
                    throw new RuntimeException("Please implement me");
                    //for(i=0; i<aux_comp_count; i++) {
                    //   boolean  load_intra_quant_mat_grayscale  = brb.readBool();
                    //  if(load_intra_quant_mat_grayscale)
                    //       intra_quant_mat_grayscale[i]  8*[2-64] uimsbf
                    //    boolean load_nonintra_quant_mat_grayscale = brb.readBool();
                    //    if(load_nonintra_quant_mat_grayscale)
                    //        nonintra_quant_mat_grayscale[i]  8*[2-64] uimsbf
                    //}
                }
            }

            if (video_object_layer_verid != 1) {
                boolean quarter_sample = brb.readBool();
            }
            boolean complexity_estimation_disable = brb.readBool();

            if (!complexity_estimation_disable) {
                throw new RuntimeException("Please implement me");
                // define_vop_complexity_estimation_header()
            }
            boolean resync_marker_disable = brb.readBool();
            boolean data_partitioned = brb.readBool();
            if (data_partitioned) {
                boolean reversible_vlc = brb.readBool();
            }
            if (video_object_layer_verid != 1) {
                boolean newpred_enable = brb.readBool();
                if (newpred_enable) {
                    int requested_upstream_message_type = brb.readBits(2);
                    boolean newpred_segment_type = brb.readBool();
                }
                boolean reduced_resolution_vop_enable = brb.readBool();
            }
            boolean scalability = brb.readBool();
            if (scalability) {
                boolean hierarchy_type = brb.readBool();
                int ref_layer_id = brb.readBits(4);
                boolean ref_layer_sampling_direc = brb.readBool();
                int hor_sampling_factor_n = brb.readBits(5);
                int hor_sampling_factor_m = brb.readBits(5);
                int vert_sampling_factor_n = brb.readBits(5);
                int vert_sampling_factor_m = brb.readBits(5);
                boolean enhancement_type = brb.readBool();
                if (video_object_layer_shape == 1 && !hierarchy_type) {
                    boolean use_ref_shape = brb.readBool();
                    boolean use_ref_texture = brb.readBool();
                    int shape_hor_sampling_factor_n = brb.readBits(5);
                    int shape_hor_sampling_factor_m = brb.readBits(5);
                    int shape_vert_sampling_factor_n = brb.readBits(5);
                    int shape_vert_sampling_factor_m = brb.readBits(5);
                }
            }*/
        } else {
            throw new RuntimeException("Please implmenet me");
        }
    }

    protected Sample createSampleObject(List<? extends ByteBuffer> nals) {
        ByteBuffer startcode = ByteBuffer.wrap(new byte[]{0, 0, 1});
        ByteBuffer[] data = new ByteBuffer[nals.size() * 2];
        for (int i = 0; i < nals.size(); i++) {
            data[2 * i] = startcode;
            data[2 * i + 1] = nals.get(i);
        }
        return new SampleImpl(data, mp4v);
    }

    public List<SampleEntry> getSampleEntries() {

        return Collections.<SampleEntry>singletonList(mp4v);
    }

    public String getHandler() {
        return "vide";
    }

    public List<Sample> getSamples() {
        return samples;
    }
}
