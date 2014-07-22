/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.mp4parser.util;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;


public class ChannelHelper {
    private static ByteBuffer empty = ByteBuffer.allocate(0).asReadOnlyBuffer();



    public static void readFully(final ReadableByteChannel channel, final ByteBuffer buf)
            throws IOException {
        readFully(channel, buf, buf.remaining());
    }

    public static int readFully(final ReadableByteChannel channel, final ByteBuffer buf, final int length)
            throws IOException {
        int n, count = 0;
        while (-1 != (n = channel.read(buf))) {
            count += n;
            if (count == length) {
                break;
            }
        }
        if (n == -1) {
            throw new EOFException("End of file. No more boxes.");
        }
        return count;
    }



}