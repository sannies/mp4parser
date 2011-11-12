/*
Copyright (c) 2011 Stanislav Vitvitskiy

Permission is hereby granted, free of charge, to any person obtaining a copy of this
software and associated documentation files (the "Software"), to deal in the Software
without restriction, including without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
OR OTHER DEALINGS IN THE SOFTWARE.
*/
package com.googlecode.mp4parser.h264.model;


import com.coremedia.iso.IsoBufferWrapper;
import com.googlecode.mp4parser.h264.read.CAVLCReader;
import com.googlecode.mp4parser.h264.write.CAVLCWriter;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Supplementary Enhanced Information entity of H264 bitstream
 * <p/>
 * capable to serialize and deserialize with CAVLC bitstream
 *
 * @author Stanislav Vitvitskiy
 */
public class SEI extends BitstreamElement {

    public static final int BUFFERING_PERIOD = 0;
    public static final int PIC_TIMING = 1;
    public static final int PAN_SCAN_RECT = 2;
    public static final int FILLER_PAYLOAD = 3;
    public static final int USER_DATA_REGISTERED_ITU_T_T35 = 4;
    public static final int USER_DATA_UNREGISTERED = 5;
    public static final int RECOVERY_POINT = 6;
    public static final int DEC_REF_PIC_MARKING_REPETITION = 7;
    public static final int SPARE_PIC = 8;
    public static final int SCENE_INFO = 9;
    public static final int SUB_SEQ_INFO = 10;
    public static final int SUB_SEQ_LAYER_CHARACTERISTICS = 11;
    public static final int SUB_SEQ_CHARACTERISTICS = 12;
    public static final int FULL_FRAME_FREEZE = 13;
    public static final int FULL_FRAME_FREEZE_RELEASE = 14;
    public static final int FULL_FRAME_SNAPSHOT = 15;
    public static final int PROGRESSIVE_REFINEMENT_SEGMENT_START = 16;
    public static final int PROGRESSIVE_REFINEMENT_SEGMENT_END = 17;
    public static final int MOTION_CONSTRAINED_SLICE_GROUP_SET = 18;
    public static final int FILM_GRAIN_CHARACTERISTICS = 19;
    public static final int DEBLOCKING_FILTER_DISPLAY_PREFERENCE = 20;
    public static final int STEREO_VIDEO_INFO = 21;
    public static final int POST_FILTER_HINT = 22;
    public static final int TONE_MAPPING_INFO = 23;
    public static final int SCALABILITY_INFO = 24;
    public static final int SUB_PIC_SCALABLE_LAYER = 25;
    public static final int NON_REQUIRED_LAYER_REP = 26;
    public static final int PRIORITY_LAYER_INFO = 27;
    public static final int LAYERS_NOT_PRESENT = 28;
    public static final int LAYER_DEPENDENCY_CHANGE = 29;
    public static final int SCALABLE_NESTING = 30;
    public static final int BASE_LAYER_TEMPORAL_HRD = 31;
    public static final int QUALITY_LAYER_INTEGRITY_CHECK = 32;
    public static final int REDUNDANT_PIC_PROPERTY = 33;
    public static final int TL0_DEP_REP_INDEX = 34;
    public static final int TL_SWITCHING_POINT = 35;
    public static final int PARALLEL_DECODING_INFO = 36;
    public static final int MVC_SCALABLE_NESTING = 37;
    public static final int VIEW_SCALABILITY_INFO = 38;
    public static final int MULTIVIEW_SCENE_INFO = 39;
    public static final int MULTIVIEW_ACQUISITION_INFO = 40;
    public static final int NON_REQUIRED_VIEW_COMPONENT = 41;
    public static final int VIEW_DEPENDENCY_CHANGE = 42;
    public static final int OPERATION_POINTS_NOT_PRESENT = 43;
    public static final int BASE_VIEW_TEMPORAL_HRD = 44;
    public static final int FRAME_PACKING_ARRANGEMENT = 45;

    public static class SEIMessage {
        public int payloadType;
        public int payloadSize;
        public byte[] payload;

        public SEIMessage(int payloadType2, int payloadSize2, byte[] payload2) {
            this.payload = payload2;
            this.payloadType = payloadType2;
            this.payloadSize = payloadSize2;
        }

    }

    public SEIMessage[] messages;

    public SEI(SEIMessage[] messages) {
        this.messages = messages;
    }

    public static SEI read(IsoBufferWrapper is) throws IOException {
        CAVLCReader reader = new CAVLCReader(is);

        ArrayList<SEIMessage> messages = new ArrayList<SEIMessage>();
        do {
            messages.add(sei_message(reader));
        } while (reader.moreRBSPData());

        reader.readTrailingBits();

        return new SEI(messages.toArray(new SEIMessage[]{}));
    }

    private static SEIMessage sei_message(CAVLCReader reader)
            throws IOException {
        int payloadType = 0;
        while (reader.peakNextBits(8) == 0xFF) {
            reader.readNBit(8);
            payloadType += 255;
        }
        int last_payload_type_byte = (int) reader.readNBit(8,
                "SEI: last_payload_type_byte");
        payloadType += last_payload_type_byte;
        int payloadSize = 0;
        while (reader.peakNextBits(8) == 0xFF) {
            reader.readNBit(8);
            payloadSize += 255;
        }
        int last_payload_size_byte = (int) reader.readNBit(8,
                "SEI: last_payload_size_byte");
        payloadSize += last_payload_size_byte;
        byte[] payload = sei_payload(payloadType, payloadSize, reader);

        return new SEIMessage(payloadType, payloadSize, payload);

    }

    private static byte[] sei_payload(int payloadType, int payloadSize,
                                      CAVLCReader reader) throws IOException {
        return reader.read(payloadSize);
    }

    public void write(OutputStream out) throws IOException {
        CAVLCWriter writer = new CAVLCWriter(out);
        // TODO Auto-generated method stub

        writer.writeTrailingBits();

    }

    @Override
    public String toString() {
        String messagesString = "";
        for (SEIMessage message : messages) {
            messagesString += SEIMessageToStringer.toString(message) + ", ";
        }
        return "SEI{" +
                "messages=" + messagesString + '}';
    }
}