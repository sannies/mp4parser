package com.googlecode.mp4parser;

import com.coremedia.iso.IsoTypeReaderVariable;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.WrappingTrack;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.TextTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.h264.H264NalUnitHeader;
import com.googlecode.mp4parser.authoring.tracks.h264.H264NalUnitTypes;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;
import com.googlecode.mp4parser.authoring.tracks.h264.SliceHeader;
import com.googlecode.mp4parser.authoring.tracks.webvtt.WebVttTrack;
import com.googlecode.mp4parser.h264.model.PictureParameterSet;
import com.googlecode.mp4parser.h264.model.SeqParameterSet;
import com.googlecode.mp4parser.h264.read.CAVLCReader;
import com.googlecode.mp4parser.srt.SrtParser;
import com.googlecode.mp4parser.util.Mp4Arrays;
import com.googlecode.mp4parser.util.Path;
import com.mp4parser.iso14496.part15.AvcConfigurationBox;
import com.mp4parser.iso23001.part7.CencSampleAuxiliaryDataFormat;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Adds subtitles.
 */
public class SubTitleExample {
    public static void main(String[] args) throws IOException {
        Movie m = new Movie();
        String bd = "C:\\dev\\DRMTODAY-872\\";

        //Track eng = MovieCreator.build(bd  + "31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_128000_eng.mp4").getTracks().get(0);
        // Track eng = MovieCreator.build("C:\\dev\\mp4parser\\31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_128000_eng.mp4").getTracks().get(0);
        //m.addTrack(eng);

        //Track vid = MovieCreator.build(bd  + "31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_600000.mp4").getTracks().get(0);
        //Track vid = MovieCreator.build("C:\\content\\843D111F-E839-4597-B60C-3B8114E0AA72_ABR01.mp4").getTracks().get(0);
        Track vid = new H264TrackImpl(new FileDataSourceImpl("C:\\dev\\mp4parser\\check-b-frames.h264"), "eng", 25, 1);
        //Track vid = MovieCreator.build(new FileDataSourceImpl("C:\\dev\\mp4parser\\tos-vid.mp4")).getTracks().get(0);//, "eng", 25, 1);
        m.addTrack(vid);


        long refSize = 0;
        long fullSize = 0;

        final List<Sample> nuSamples = new ArrayList<Sample>();

        List<CompositionTimeToSample.Entry> nuCompOffsets = new ArrayList<CompositionTimeToSample.Entry>();
        long[] nuDurations = new long[0];
        int[] compOffsets = CompositionTimeToSample.blowupCompositionTimes(vid.getCompositionTimeEntries());
        long[] nuSyncSamples = new long[0];
        long durationAddon = 0;
        AvcConfigurationBox avcC = Path.getPath((Container) vid.getSampleDescriptionBox(), "avc1/avcC");
        PictureParameterSet pps = PictureParameterSet.read(avcC.getPictureParameterSets().get(0));
        // ppss.get(pic_parameter_set_id);
        SeqParameterSet sps = SeqParameterSet.read(avcC.getSequenceParameterSets().get(0));

        System.err.println(sps.pic_order_cnt_type);

        for (int i = 0; i < vid.getSamples().size(); i++) {
            Sample sample = vid.getSamples().get(i);
            fullSize += sample.getSize();
            ByteBuffer bb = sample.asByteBuffer();
            int nalRefIdc = 0;
            boolean syncSample = false;
            boolean isP = false;
            while (bb.remaining() > 0) {
                int nalLength = l2i(IsoTypeReaderVariable.read(bb, 4));
                ByteBuffer nal = (ByteBuffer) bb.slice().limit(nalLength);

                H264NalUnitHeader nuh = H264TrackImpl.getNalUnitHeader(nal);
                switch (nuh.nal_unit_type) {
                    case H264NalUnitTypes.CODED_SLICE_NON_IDR:
                    case H264NalUnitTypes.CODED_SLICE_DATA_PART_A:
                    case H264NalUnitTypes.CODED_SLICE_DATA_PART_B:
                    case H264NalUnitTypes.CODED_SLICE_DATA_PART_C:
                    case H264NalUnitTypes.CODED_SLICE_IDR:

                        byte[] restOfNal = new byte[nal.remaining()];
                        nal.get(restOfNal);
                        CAVLCReader reader = new CAVLCReader(new ByteArrayInputStream(restOfNal, 1, restOfNal.length - 1));
                        int first_mb_in_slice = reader.readUE("SliceHeader: first_mb_in_slice");
                        int sliceTypeInt = reader.readUE("SliceHeader: slice_type");

                        switch (sliceTypeInt % 5) {
                            case 0:
                                System.out.print("P " + nuh.nal_ref_idc + " " + nuh.nal_unit_type + " ");
                                isP = true;
                                break;
                            case 1:
                                System.out.print("B " + nuh.nal_ref_idc + " " + nuh.nal_unit_type + " ");
                                break;
                            case 2:
                                System.out.print("I " + nuh.nal_ref_idc + " " + nuh.nal_unit_type + " ");
                                break;
                            case 3:
                                System.out.print("SP " + nuh.nal_ref_idc + " " + nuh.nal_unit_type + " ");
                                isP = true;
                                break;
                            case 4:
                                System.out.print("SI " + nuh.nal_ref_idc + " " + nuh.nal_unit_type + " ");
                                break;

                        }


                        PictureParameterSet.read(avcC.getPictureParameterSets().get(0));
                        int pic_parameter_set_id = reader.readUE("SliceHeader: pic_parameter_set_id");
                        //SeqParameterSet sps = spss.get(pps.seq_parameter_set_id);
                        if (sps.residual_color_transform_flag) {
                            int colour_plane_id = reader.readU(2, "SliceHeader: colour_plane_id");
                        }
                        int frame_num = reader.readU(sps.log2_max_frame_num_minus4 + 4, "SliceHeader: frame_num");
                        //System.out.print(" frameNum " + frame_num + " ");
                        // System.out.print(sliceTypeInt + " ");
                        boolean field_pic_flag = false;
                        boolean bottom_field_flag = false;
                        if (!sps.frame_mbs_only_flag) {
                            field_pic_flag = reader.readBool("SliceHeader: field_pic_flag");
                            if (field_pic_flag) {
                                bottom_field_flag = reader.readBool("SliceHeader: bottom_field_flag");
                            }
                        }
                        if (nuh.nal_unit_type == H264NalUnitTypes.CODED_SLICE_IDR) {

                            int idr_pic_id = reader.readUE("SliceHeader: idr_pic_id");
                        }
                        if (sps.pic_order_cnt_type == 0) {
                            int pic_order_cnt_lsb = reader.readU(sps.log2_max_pic_order_cnt_lsb_minus4 + 4, "SliceHeader: pic_order_cnt_lsb");

                            int max_pic_order_count = (1 << (sps.log2_max_pic_order_cnt_lsb_minus4 + 4));
                            // System.out.print(" pic_order_cnt_lsb " + pic_order_cnt_lsb + " " + max_pic_order_count);
                            int pic_order_cnt = pic_order_cnt_lsb;
                            while (pic_order_cnt+(max_pic_order_count/2)<i) {
                                pic_order_cnt += max_pic_order_count;
                            }
                            System.out.print("dec" + i + " poc " + pic_order_cnt + " " + (pic_order_cnt - i) + " ");
                            if (pps.bottom_field_pic_order_in_frame_present_flag && !field_pic_flag) {
                                int delta_pic_order_cnt_bottom = reader.readSE("SliceHeader: delta_pic_order_cnt_bottom");
                            }
                        }

                        if (sps.pic_order_cnt_type == 1 && !sps.delta_pic_order_always_zero_flag) {

                            int delta_pic_order_cnt_0 = reader.readSE("delta_pic_order_cnt_0");
                            if (pps.bottom_field_pic_order_in_frame_present_flag && !field_pic_flag) {
                                int delta_pic_order_cnt_1 = reader.readSE("delta_pic_order_cnt_1");
                            }
                        }


                }
                System.out.println();
                //System.out.print("NAL Unit Type " + nuh.nal_unit_type + " Ref Idc " + nuh.nal_ref_idc + " | ");
                bb.position(bb.position() + nalLength);
                if (nuh.nal_unit_type == 5) {
                    syncSample = true;
                }
            }

            if (!isP) {
                refSize += sample.getSize();
                nuSamples.add(sample);
                nuDurations = Mp4Arrays.copyOfAndAppend(nuDurations, vid.getSampleDurations()[i] + durationAddon);
                nuCompOffsets.add(new CompositionTimeToSample.Entry(1, compOffsets[i]));
                if (syncSample) {
                    nuSyncSamples = Mp4Arrays.copyOfAndAppend(nuSyncSamples, nuSamples.size());
                }
                durationAddon = 0;
            } else {
                durationAddon += vid.getSampleDurations()[i];
            }
//            System.out.println("---------------");

        }
        System.out.println("" + refSize + " vs. " + fullSize);

        Track sub = new WebVttTrack(new FileInputStream(bd + "31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_deu.vtt"), "subs", Locale.GERMAN);
        //m.addTrack(sub);

        Container c = new DefaultMp4Builder().build(m);
        c.writeContainer(new FileOutputStream("output.mp4").getChannel());


        final long[] finalNuDurations = nuDurations;
        final long[] finalNuSyncSamples = nuSyncSamples;
        WrappingTrack wrappingTrack = new WrappingTrack(vid) {
            @Override
            public long[] getSampleDurations() {
                return finalNuDurations;
            }

            @Override
            public long[] getSyncSamples() {
                return finalNuSyncSamples;
            }

            @Override
            public List<Sample> getSamples() {
                return nuSamples;
            }
        };

        Movie m2 = new Movie();
        m2.addTrack(wrappingTrack);
        //m2.addTrack(eng);
        Container c2 = new DefaultMp4Builder().build(m2);
        c2.writeContainer(new FileOutputStream("output-small.mp4").getChannel());


    }

}
