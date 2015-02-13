package com.googlecode.mp4parser.authoring.tracks.h265;

import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer;
import com.googlecode.mp4parser.h264.read.BitstreamReader;

import java.io.IOException;

/**
 * Created by sannies on 03.02.2015.
 */
public class SEIMessage {
    public SEIMessage(BitReaderBuffer bsr) throws IOException {
        int payloadType = 0;
        long ff_byte;
        while( (ff_byte = bsr.readBits(8))  == 0xFF ) {
            payloadType += 255;
        }
        int last_payload_type_byte = (int) bsr.readBits(8);
        payloadType += last_payload_type_byte;
        int payloadSize = 0;
        while( (ff_byte = bsr.readBits(8))  == 0xFF ) {
            payloadSize += 255;
        }
        int last_payload_size_byte = (int) bsr.readBits(8);
        payloadSize += last_payload_size_byte;
        System.err.println("payloadType " + payloadType);
        //sei_payload(payloadType, payloadSize );
    }
}
