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

/**
 * Supplementary Enhanced Information entity of H264 bitstream
 * <p/>
 * capable to serialize and deserialize with CAVLC bitstream
 *
 * @author Stanislav Vitvitskiy
 */
public class SEI extends BitstreamElement {

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
}