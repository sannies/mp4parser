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
package com.mp4parser.tools;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

/**
 * Creates a <code>ReadableByteChannel</code> that is backed by a <code>ByteBuffer</code>.
 */
public class ByteBufferByteChannel implements ByteChannel {
    ByteBuffer byteBuffer;

    public ByteBufferByteChannel(byte[] byteArray) {
        this(ByteBuffer.wrap(byteArray));
    }

    public ByteBufferByteChannel(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
    }

    public int read(ByteBuffer dst) throws IOException {
        int rem = dst.remaining();
        if (byteBuffer.remaining() <= 0) {
            return -1;
        }
        dst.put((ByteBuffer) byteBuffer.duplicate().limit(byteBuffer.position() + dst.remaining()));
        byteBuffer.position(byteBuffer.position() + rem);
        return rem;
    }

    public boolean isOpen() {
        return true;
    }

    public void close() throws IOException {
    }

    public int write(ByteBuffer src) throws IOException {
        int r = src.remaining();
        byteBuffer.put(src);
        return r;
    }
}
