package org.mp4parser.muxer.tracks.h265;

/**
 * Created by sannies on 02.01.2015.
 */
public interface H265NalUnitTypes {
    final int NAL_TYPE_TRAIL_N = 0;
    final int NAL_TYPE_TRAIL_R = 1;
    final int NAL_TYPE_TSA_N = 2;
    final int NAL_TYPE_TSA_R = 3;
    final int NAL_TYPE_STSA_N = 4;
    final int NAL_TYPE_STSA_R = 5;
    final int NAL_TYPE_RADL_N = 6;
    final int NAL_TYPE_RADL_R = 7;
    final int NAL_TYPE_RASL_N = 8;
    final int NAL_TYPE_RASL_R = 9;

    final int NAL_TYPE_RSV_VCL_N10 = 10;
    final int NAL_TYPE_RSV_VCL_N12 = 12;
    final int NAL_TYPE_RSV_VCL_N14 = 14;

    final int NAL_TYPE_RSV_VCL_R11 = 11;
    final int NAL_TYPE_RSV_VCL_R13 = 13;
    final int NAL_TYPE_RSV_VCL_R15 = 15;


    final int NAL_TYPE_BLA_W_LP = 16;
    final int NAL_TYPE_BLA_W_RADL = 17;
    final int NAL_TYPE_BLA_N_LP = 18;
    final int NAL_TYPE_IDR_W_RADL = 19;
    final int NAL_TYPE_IDR_N_LP = 20;
    final int NAL_TYPE_CRA_NUT = 21;
    final int NAL_TYPE_RSV_IRAP_VCL22 = 22;
    final int NAL_TYPE_RSV_IRAP_VCL23 = 23;
    final int NAL_TYPE_RSV_VCL24 = 24;
    final int NAL_TYPE_RSV_VCL25 = 25;
    final int NAL_TYPE_RSV_VCL26 = 26;
    final int NAL_TYPE_RSV_VCL27 = 27;
    final int NAL_TYPE_RSV_VCL28 = 28;
    final int NAL_TYPE_RSV_VCL29 = 29;
    final int NAL_TYPE_RSV_VCL30 = 30;
    final int NAL_TYPE_RSV_VCL31 = 31;

    final int NAL_TYPE_VPS_NUT = 32;
    final int NAL_TYPE_SPS_NUT = 33;
    final int NAL_TYPE_PPS_NUT = 34;
    final int NAL_TYPE_AUD_NUT = 35;
    final int NAL_TYPE_EOS_NUT = 36;
    final int NAL_TYPE_EOB_NUT = 37;
    final int NAL_TYPE_FD_NUT = 38;

    final int NAL_TYPE_PREFIX_SEI_NUT = 39;
    final int NAL_TYPE_RSV_NVCL41 = 41;
    final int NAL_TYPE_RSV_NVCL42 = 42;
    final int NAL_TYPE_RSV_NVCL43 = 43;
    final int NAL_TYPE_RSV_NVCL44 = 44;
    final int NAL_TYPE_UNSPEC48 = 48;
    final int NAL_TYPE_UNSPEC49 = 49;
    final int NAL_TYPE_UNSPEC50 = 50;
    final int NAL_TYPE_UNSPEC51 = 51;
    final int NAL_TYPE_UNSPEC52 = 52;
    final int NAL_TYPE_UNSPEC53 = 53;
    final int NAL_TYPE_UNSPEC54 = 54;
    final int NAL_TYPE_UNSPEC55 = 55;

}
