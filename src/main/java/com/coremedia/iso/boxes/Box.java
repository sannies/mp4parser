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

package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.googlecode.mp4parser.DataSource;

import java.nio.channels.WritableByteChannel;

/**
 * Defines basic interaction possibilities for any ISO box. Each box has a parent box and a type.
 */
public interface Box {
    Container getParent();

    void setParent(Container parent);

    long getSize();

    /**
     * Returns the position of the box in the original file.
     *
     * @return the start offset in the source file
     */
    long getOffset();

    /**
     * The box's 4-cc type.
     *
     * @return the 4 character type of the box
     */
    String getType();

    /**
     * Writes the complete box - size | 4-cc | content - to the given <code>writableByteChannel</code>.
     *
     * @param writableByteChannel the box's sink
     * @throws IOException in case of problems with the <code>Channel</code>
     */
    void getBox(WritableByteChannel writableByteChannel) throws IOException;

    /**
     * Parses the box excluding the already parsed header (size, 4cc, [long-size], [user-type]).
     * The remaining size of the box is the <code>contentSize</code>, <code>contentSize</code>
     * number of bytes should be read from the box source (<code>readableByteChannel</code>).
     * If you need the <code>header</code> buffer at a later stage you have to create a copy.
     *
     * @param dataSource the source for this box
     * @param header      the box' already parsed header (create copy if you need it
     *                    later as it will be overwritten)
     * @param contentSize remaining bytes of this box
     * @param boxParser   use it to parse sub-boxes.
     * @throws IOException in case of an error during a read operation
     */
    void parse(DataSource dataSource, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException;

}
