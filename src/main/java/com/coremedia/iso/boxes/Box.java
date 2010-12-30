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

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * A basic ISO box. No full box.
 */
public abstract class Box implements BoxInterface {
    public long offset;

    public long getSize() {
        return getContentSize() + getHeaderSize() + getDeadBytes().length;
    }

    protected long getHeaderSize() {
        return 4 + // size
                4 + // type
                (getContentSize() >= 4294967296L ? 8 : 0) +
                (Arrays.equals(getType(), IsoFile.fourCCtoBytes("uuid")) ? 16 : 0);
    }

    /**
     * Gets the box's content size without header size.
     *
     * @return Gets the box's content size in bytes
     */
    protected abstract long getContentSize();

    private byte[] type;
    private byte[] userType;
    private BoxContainer parent;

    protected Box(byte[] type) {
        this.type = type;
    }

    public byte[] getType() {
        return type;
    }

    public byte[] getUserType() {
        return userType;
    }

    public void setUserType(byte[] userType) {
        this.userType = userType;
    }

    public BoxContainer getParent() {
        return parent;
    }

    public long getOffset() {
        return offset;
    }

    public void setParent(BoxContainer parent) {
        this.parent = parent;
    }

    public IsoFile getIsoFile() {
        return parent.getIsoFile();
    }

    /**
     * Pareses the given IsoBufferWrapper and returns the remaining bytes.
     *
     * @param in                   the (part of the) iso file to parse
     * @param size                 expected size of the box
     * @param boxFactory           creates inner boxes
     * @param lastMovieFragmentBox
     * @throws IOException in case of an I/O error.
     */
    public abstract void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException;

    /**
     * Returns the human readable name of the box.
     *
     * @return a display string
     */
    public abstract String getDisplayName();


    ByteBuffer[] deadBytes = new ByteBuffer[]{};

    public ByteBuffer[] getDeadBytes() {
        return deadBytes;
    }

    public void setDeadBytes(ByteBuffer[] newDeadBytes) {
        deadBytes = newDeadBytes;
    }

    public byte[] getHeader() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IsoOutputStream ios = new IsoOutputStream(baos);
            if (isSmallBox()) {
                ios.writeUInt32((int) this.getSize());
                ios.write(getType());
            } else {
                ios.writeUInt32(1);
                ios.write(getType());
                ios.writeUInt64(getSize());
            }
            if (Arrays.equals(getType(), IsoFile.fourCCtoBytes("uuid"))) {
                ios.write(userType);
            }

            assert baos.size() == getHeaderSize();
            return baos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isSmallBox() {
        return this.getSize() < 4294967296L;
    }

    public void getBox(IsoOutputStream os) throws IOException {
        long sp = os.getStreamPosition();
        os.write(getHeader());
        getContent(os);
        for (ByteBuffer buffer : deadBytes) {
            buffer.rewind();
            byte[] bufAsAr = new byte[buffer.limit()];
            buffer.get(bufAsAr);
            os.write(bufAsAr);
        }

        long writtenBytes = os.getStreamPosition() - sp;
        assert writtenBytes == getSize() : " getHeader + getContent + getDeadBytes (" + writtenBytes + ") of "
                + IsoFile.bytesToFourCC(getType())
                + " doesn't match getSize (" + getSize() + ")";

    }

    /**
     * Writes the box's content into the given <code>IsoOutputStream</code>. This MUST NOT include
     * any header bytes.
     *
     * @param os the box's content-sink.
     * @throws IOException in case of an exception in the underlying <code>OutputStream</code>.
     */
    protected abstract void getContent(IsoOutputStream os) throws IOException;

    public static int utf8StringLengthInBytes(String utf8) {
        try {
            if (utf8 != null) {
                return utf8.getBytes("UTF-8").length;
            } else {
                return 0;
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException();
        }
    }


    public long calculateOffset() {
        //todo: doesn't work for fragmented files as it doesn't take mdats into account (as they are not in the parent structure)
        long offsetFromParentBoxStart = parent.getNumOfBytesToFirstChild();
        Box[] boxes = this.getParent().getBoxes();
        for (Box box : boxes) {
            if (box.equals(this)) {
                return parent.calculateOffset() + offsetFromParentBoxStart;
            }
            offsetFromParentBoxStart += box.getSize();
        }
        throw new RuntimeException("this box is not in the list of its parent's children");
    }
}
