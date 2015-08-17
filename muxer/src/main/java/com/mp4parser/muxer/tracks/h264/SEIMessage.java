package com.mp4parser.muxer.tracks.h264;

import com.mp4parser.muxer.tracks.h264.parsing.model.SeqParameterSet;
import com.mp4parser.muxer.tracks.h264.parsing.read.CAVLCReader;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by sannies on 15.08.2015.
 */
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
