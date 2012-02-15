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
package com.googlecode.mp4parser.h264;

import com.coremedia.iso.IsoBufferWrapper;

import java.io.IOException;

/**
 * Input stream that automatically unwraps NAL units and allows for NAL unit
 * navigation
 *
 * @author Stanislav Vitvitskiy
 */
public class IsoSampleNALUnitReader implements NALUnitReader {
    private final IsoBufferWrapper src;
    private int nalLengthSize = 4;

    public IsoSampleNALUnitReader(IsoBufferWrapper src, int nalLengthSize) throws IOException {
        this.src = src;
        this.nalLengthSize = nalLengthSize;
    }


    public IsoBufferWrapper nextNALUnit() throws IOException {
        if (src.remaining() < 5) {
            return null;
        }

        long nalLength;
        if (src.remaining() >= nalLengthSize) {
            //nalLength = src.read(nalLengthSize);
            nalLength = src.readUInt32();
            if (nalLength == 0) return null;
            
            IsoBufferWrapper segment = src.getSegment(src.position(), nalLength);
            src.position(src.position() + nalLength);
            return segment;
        } else {
            throw new RuntimeException("remaining bytes less than nalLengthSize found in sample. should not be here.");
        }
    }
}