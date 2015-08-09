package com.mp4parser.muxer.tracks.h264;

/**
 * Created by sannies on 18.05.2015.
 */
public interface H264NalUnitTypes {
    int UNSPECIFIED =  0;
    int CODED_SLICE_NON_IDR = 1;
    int CODED_SLICE_DATA_PART_A = 2;
    int CODED_SLICE_DATA_PART_B = 3;
    int CODED_SLICE_DATA_PART_C = 4;
    int CODED_SLICE_IDR = 5;
    int SEI = 6;
    int SEQ_PARAMETER_SET = 7;
    int PIC_PARAMETER_SET = 8;
    int AU_UNIT_DELIMITER = 9;
    int END_OF_SEQUENCE = 10;
    int END_OF_STREAM = 11;
    int FILLER_DATA = 12;
    int SEQ_PARAMETER_SET_EXT = 13;
    int PREFIX_NAL_UNIT = 14;
    int SUBSET_SEQ_PARAMETER_SET = 15;
    int RESERVERED_16 = 16;
    int RESERVERED_17 = 17;
    int RESERVERED_18 = 18;
    int CODED_SLICE_AUX_PIC = 19;
    int CODED_SLICE_EXT =20;
    int RESERVED_21 = 21;
    int RESERVED_22 = 22;
    int RESERVED_23 = 23;
    int UNSPEC_24 = 24;
    int UNSPEC_25 = 25;
    int UNSPEC_26 = 26;
    int UNSPEC_27 = 27;
    int UNSPEC_28 = 28;
    int UNSPEC_29 = 29;
    int UNSPEC_30 = 30;
    int UNSPEC_31 = 31;
}
