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
import java.util.Arrays;

/**
 * Input stream that automatically unwraps NAL units and allows for NAL unit
 * navigation
 *
 * @author Stanislav Vitvitskiy
 */
public class AnnexBNALUnitReader implements NALUnitReader {
    private final IsoBufferWrapper src;

    public AnnexBNALUnitReader(IsoBufferWrapper src) throws IOException {
        this.src = src;
    }


    public IsoBufferWrapper nextNALUnit() throws IOException {
        if (src.remaining() < 5) {
            return null;
        }

        long start = -1;
        do {
            byte[] marker = new byte[4];
            if (src.remaining() >= 4) {
                src.read(marker);
                if (Arrays.equals(new byte[]{0, 0, 0, 1}, marker)) {
                    if (start == -1) {
                        start = src.position();
                    } else {
                        src.position(src.position() - 4);
                        return src.getSegment(start, src.position() - start);
                    }
                } else {
                    src.position(src.position() - 3); // advance 1 byte at a time
                }
            } else {
                return src.getSegment(start, src.size() - start); // rest
            }

        } while (src.remaining() > 0);
        return src.getSegment(start, src.position());

    }
}