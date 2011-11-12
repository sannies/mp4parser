package com.googlecode.mp4parser.h264.model;

/**
 *
 */
public class SEIMessageToStringer {
    static SEIMessageToStringer seiMessageToStringer = new SEIMessageToStringer();
    
    
    public static String toString(SEI.SEIMessage message) {
        return seiMessageToStringer.toString_(message);
    }
    
    private String toString_(SEI.SEIMessage message) {

        int payloadType = message.payloadType;
        if (payloadType == 0) {
            return buffering_period(message.payload);
        } else if (payloadType == 1) {
            return pic_timing(message.payload);
        } else if (payloadType == 2) {
            return pan_scan_rect(message.payload);
        } else if (payloadType == 3) {
            return filler_payload(message.payload);
        } else if (payloadType == 4) {
            return user_data_registered_itu_t_t35(message.payload);
        } else if (payloadType == 5) {
            return user_data_unregistered(message.payload);
        } else if (payloadType == 6) {
            return recovery_point(message.payload);
        } else if (payloadType == 7) {
            return dec_ref_pic_marking_repetition(message.payload);
        } else if (payloadType == 8) {
            return spare_pic(message.payload);
        } else if (payloadType == 9)
            return scene_info(message.payload);
        else if (payloadType == 10)
            return sub_seq_info(message.payload);
        else if (payloadType == 11)
            return sub_seq_layer_characteristics(message.payload);
        else if (payloadType == 12)
            return sub_seq_characteristics(message.payload);
        else if (payloadType == 13)
            return full_frame_freeze(message.payload);
        else if (payloadType == 14)
            return full_frame_freeze_release(message.payload);
        else if (payloadType == 15)
            return full_frame_snapshot(message.payload);
        else if (payloadType == 16)
            return progressive_refinement_segment_start(message.payload);
        else if (payloadType == 17)
            return progressive_refinement_segment_end(message.payload);
        else if (payloadType == 18)
            return motion_constrained_slice_group_set(message.payload);
        else if (payloadType == 19)
            return film_grain_characteristics(message.payload);
        else if (payloadType == 20)
            return deblocking_filter_display_preference(message.payload);
        else if (payloadType == 21)
            return stereo_video_info(message.payload);
        else if (payloadType == 22)
            return post_filter_hint(message.payload);
        else if (payloadType == 23)
            return tone_mapping_info(message.payload);
        else if (payloadType == 24)
            return scalability_info(message.payload); /* specified in Annex G */
        else if (payloadType == 25)
            return sub_pic_scalable_layer(message.payload); /* specified in Annex G */
        else if (payloadType == 26)
            return non_required_layer_rep(message.payload); /* specified in Annex G */
        else if (payloadType == 27)
            return priority_layer_info(message.payload); /* specified in Annex G */
        else if (payloadType == 28)
            return layers_not_present(message.payload); /* specified in Annex G */
        else if (payloadType == 29)
            return layer_dependency_change(message.payload); /* specified in Annex G */
        else if (payloadType == 30)
            return scalable_nesting(message.payload); /* specified in Annex G */
        else if (payloadType == 31)
            return base_layer_temporal_hrd(message.payload); /* specified in Annex G */
        else if (payloadType == 32)
            return quality_layer_integrity_check(message.payload); /* specified in Annex G */
        else if (payloadType == 33)
            return redundant_pic_property(message.payload); /* specified in Annex G */
        else if (payloadType == 34)
            return tl0_dep_rep_index(message.payload); /* specified in Annex G */
        else if (payloadType == 35)
            return tl_switching_point(message.payload); /* specified in Annex G */
        else if (payloadType == 36)
            return parallel_decoding_info(message.payload); /* specified in Annex H */
        else if (payloadType == 37)
            return mvc_scalable_nesting(message.payload); /* specified in Annex H */
        else if (payloadType == 38)
            return view_scalability_info(message.payload); /* specified in Annex H */
        else if (payloadType == 39)
            return multiview_scene_info(message.payload); /* specified in Annex H */
        else if (payloadType == 40)
            return multiview_acquisition_info(message.payload); /* specified in Annex H */
        else if (payloadType == 41)
            return non_required_view_component(message.payload); /* specified in Annex H */
        else if (payloadType == 42)
            return view_dependency_change(message.payload); /* specified in Annex H */
        else if (payloadType == 43)
            return operation_points_not_present(message.payload); /* specified in Annex H */

        else if (payloadType == 44)
            return base_view_temporal_hrd(message.payload); /* specified in Annex H */
        else if (payloadType == 45)
            return frame_packing_arrangement(message.payload);
        else
            return reserved_sei_message(message.payload);
    }


    private String progressive_refinement_segment_start(byte[] payload) {
        return "progressive_refinement_segment_start";
    }

    private String full_frame_snapshot(byte[] payload) {
        return "full_frame_snapshot";
    }

    private String full_frame_freeze_release(byte[] payload) {
        return "full_frame_freeze_release";
    }


    private String full_frame_freeze(byte[] payload) {
        return "full_frame_freeze";
    }


    private String sub_seq_characteristics(byte[] payload) {
        return "sub_seq_characteristics";
    }

    private String sub_seq_layer_characteristics(byte[] payload) {
        return "sub_seq_layer_characteristics";
    }

    private String sub_seq_info(byte[] payload) {
        return "sub_seq_info";
    }

    private String spare_pic(byte[] payload) {
        return "spare_pic";
    }

    private String dec_ref_pic_marking_repetition(byte[] payload) {
        return "dec_ref_pic_marking_repetition";
    }

    private String recovery_point(byte[] payload) {
        return "recovery_point";
    }

    private String progressive_refinement_segment_end(byte[] payload) {
        return "progressive_refinement_segment_end";
    }

    private String motion_constrained_slice_group_set(byte[] payload) {
        return "motion_constrained_slice_group_set";
    }

    private String film_grain_characteristics(byte[] payload) {
        return "film_grain_characteristics";
    }

    private String user_data_unregistered(byte[] payload) {
        return "user_data_unregistered";
    }

    private String deblocking_filter_display_preference(byte[] payload) {
        return "deblocking_filter_display_preference";
    }

    private String stereo_video_info(byte[] payload) {
        return "stereo_video_info";
    }

    private String post_filter_hint(byte[] payload) {
        return "post_filter_hint";
    }

    private String tone_mapping_info(byte[] payload) {
        return "tone_mapping_info";
    }

    private String scalability_info(byte[] payload) {
        return "scalability_info";
    }

    private String sub_pic_scalable_layer(byte[] payload) {
        return "sub_pic_scalable_layer";
    }

    private String non_required_layer_rep(byte[] payload) {
        return "non_required_layer_rep";
    }

    private String priority_layer_info(byte[] payload) {
        return "priority_layer_info";
    }

    private String user_data_registered_itu_t_t35(byte[] payload) {
        return "user_data_registered_itu_t_t35";
    }

    private String layers_not_present(byte[] payload) {
        return "layers_not_present";

    }

    private String layer_dependency_change(byte[] payload) {
        return "layer_dependency_change";
    }

    private String scalable_nesting(byte[] payload) {
        return "scalable_nesting";
    }

    private String base_layer_temporal_hrd(byte[] payload) {
        return "base_layer_temporal_hrd";

    }

    private String quality_layer_integrity_check(byte[] payload) {
        return "quality_layer_integrity_check";
    }


    private String redundant_pic_property(byte[] payload) {
        return "redundant_pic_property";
    }

    private String tl0_dep_rep_index(byte[] payload) {
        return "tl0_dep_rep_index";
    }

    private String tl_switching_point(byte[] payload) {
        return "tl_switching_point";
    }

    private String parallel_decoding_info(byte[] payload) {
        return "parallel_decoding_info";
    }

    private String mvc_scalable_nesting(byte[] payload) {
        return "mvc_scalable_nesting";
    }

    private String view_scalability_info(byte[] payload) {
        return "view_scalability_info";
    }

    private String multiview_scene_info(byte[] payload) {
        return "multiview_scene_info";
    }

    private String multiview_acquisition_info(byte[] payload) {
        return "multiview_acquisition_info";
    }

    private String non_required_view_component(byte[] payload) {
        return "non_required_view_component";
    }

    private String view_dependency_change(byte[] payload) {
        return "view_dependency_change";
    }

    private String operation_points_not_present(byte[] payload) {
        return "operation_points_not_present";
    }

    private String base_view_temporal_hrd(byte[] payload) {
        return "base_view_temporal_hrd";
    }

    private String frame_packing_arrangement(byte[] payload) {
        return "frame_packing_arrangement";
    }

    private String scene_info(byte[] payload) {
        return "scene_info";
    }

    private String reserved_sei_message(byte[] payload) {
        return "reserved_sei_message";
    }

    private String filler_payload(byte[] payload) {
        return "filler_payload";
    }

    private String pan_scan_rect(byte[] payload) {
        return "pan_scan_rect";
    }

    private String pic_timing(byte[] payload) {
        return "pic_timing";
    }

    private String buffering_period(byte[] payload) {
        return "buffering_period";
    }
}
