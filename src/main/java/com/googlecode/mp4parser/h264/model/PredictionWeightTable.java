/* Copyright */
package com.googlecode.mp4parser.h264.model;


public class PredictionWeightTable {
    public int luma_log2_weight_denom;
    public int chroma_log2_weight_denom;
    public OffsetWeight[] luma_offset_weight_l0;
    public OffsetWeight[][] chroma_offset_weight_l0;
    public OffsetWeight[] luma_offset_weight_l1;
    public OffsetWeight[][] chroma_offset_weight_l1;

    public static class OffsetWeight {
        public int offset;
        public int weight;
    }

}
