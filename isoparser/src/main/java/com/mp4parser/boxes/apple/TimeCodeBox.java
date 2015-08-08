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
package com.mp4parser.boxes.apple;

import com.mp4parser.Box;
import com.mp4parser.Container;
import com.mp4parser.boxes.sampleentry.SampleEntry;
import com.mp4parser.support.AbstractBox;
import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.IsoTypeWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Collections;
import java.util.List;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public class TimeCodeBox extends AbstractBox implements SampleEntry, Container {
    public static final String TYPE = "tmcd";

    int timeScale;
    int frameDuration;
    int numberOfFrames;
    int reserved1;
    int reserved2;
    long flags;
    int dataReferenceIndex;
    byte[] rest = new byte[0];

    public TimeCodeBox() {
        super(TYPE);
    }


    @Override
    protected long getContentSize() {
        return 8 + 4 + 4 + 4 + 4 + 1 + 3 + rest.length;

    }

    @Override
    protected void getContent(ByteBuffer bb) {
        bb.put(new byte[]{0, 0, 0, 0, 0, 0});
        IsoTypeWriter.writeUInt16(bb, dataReferenceIndex);
        bb.putInt(reserved1);
        IsoTypeWriter.writeUInt32(bb, flags);
        bb.putInt(timeScale);
        bb.putInt(frameDuration);
        IsoTypeWriter.writeUInt8(bb, numberOfFrames);
        IsoTypeWriter.writeUInt24(bb, reserved2);
        bb.put(rest);

    }


    @Override
    protected void _parseDetails(ByteBuffer content) {
        content.position(6);// ignore 6 reserved bytes;
        dataReferenceIndex = IsoTypeReader.readUInt16(content);   // 8
        reserved1 = content.getInt();
        flags = IsoTypeReader.readUInt32(content);

        timeScale = content.getInt();
        frameDuration = content.getInt();
        numberOfFrames = IsoTypeReader.readUInt8(content);
        reserved2 = IsoTypeReader.readUInt24(content);
        rest = new byte[content.remaining()];
        content.get(rest);
    }

    public int getDataReferenceIndex() {
        return dataReferenceIndex;
    }

    public void setDataReferenceIndex(int dataReferenceIndex) {
        this.dataReferenceIndex = dataReferenceIndex;
    }


    @Override
    public String toString() {
        return "TimeCodeBox{" +
                "timeScale=" + timeScale +
                ", frameDuration=" + frameDuration +
                ", numberOfFrames=" + numberOfFrames +
                ", reserved1=" + reserved1 +
                ", reserved2=" + reserved2 +
                ", flags=" + flags +
                '}';
    }

    public int getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(int timeScale) {
        this.timeScale = timeScale;
    }

    public int getFrameDuration() {
        return frameDuration;
    }

    public void setFrameDuration(int frameDuration) {
        this.frameDuration = frameDuration;
    }

    public int getNumberOfFrames() {
        return numberOfFrames;
    }

    public void setNumberOfFrames(int numberOfFrames) {
        this.numberOfFrames = numberOfFrames;
    }

    public int getReserved1() {
        return reserved1;
    }

    public void setReserved1(int reserved1) {
        this.reserved1 = reserved1;
    }

    public int getReserved2() {
        return reserved2;
    }

    public void setReserved2(int reserved2) {
        this.reserved2 = reserved2;
    }

    public long getFlags() {
        return flags;
    }

    public void setFlags(long flags) {
        this.flags = flags;
    }

    public byte[] getRest() {
        return rest;
    }

    public void setRest(byte[] rest) {
        this.rest = rest;
    }


    public List<Box> getBoxes() {
        return Collections.emptyList();
    }

    public void setBoxes(List<? extends Box> boxes) {
        throw new RuntimeException("Time Code Box doesn't accept any children");
    }

    public <T extends Box> List<T> getBoxes(Class<T> clazz) {
        return Collections.emptyList();
    }

    public <T extends Box> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        return Collections.emptyList();
    }

    public void writeContainer(WritableByteChannel bb) throws IOException {
    }
}
