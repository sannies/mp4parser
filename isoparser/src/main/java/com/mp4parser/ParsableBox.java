/*  
 * Copyright 2008 CoreMedia AG, Hamburg
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

package com.mp4parser;

import java.io.IOException;
import java.nio.ByteBuffer;

import java.nio.channels.ReadableByteChannel;

/**
 * This box is parsable and can not only be used to write data it can as well be used to read data.
 */
public interface ParsableBox extends Box {


    /**
     * Parses the box excluding the already parsed header (size, 4cc, [long-size], [user-type]).
     * The remaining size of the box is the <code>contentSize</code>, <code>contentSize</code>
     * number of bytes should be read from the box source (<code>readableByteChannel</code>).
     * If you need the <code>header</code> buffer at a later stage you have to create a copy.
     *
     * @param dataSource  the source for this box
     * @param header      the box' already parsed header (create copy if you need it
     *                    later as it will be overwritten)
     * @param contentSize remaining bytes of this box
     * @param boxParser   use it to parse sub-boxes.
     * @throws IOException in case of an error during a read operation
     */
    void parse(ReadableByteChannel dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException;

}
