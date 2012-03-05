/*
 * Copyright 2009 castLabs GmbH, Berlin
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

package com.coremedia.iso.boxes.fragment;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.AbstractFullBox;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * aligned(8) class TrackFragmentRandomAccessBox
 * extends FullBox('tfra', version, 0) {
 * unsigned int(32) track_ID;
 * const unsigned int(26) reserved = 0;
 * unsigned int(2) length_size_of_traf_num;
 * unsigned int(2) length_size_of_trun_num;
 * unsigned int(2) length_size_of_sample_num;
 * unsigned int(32) number_of_entry;
 * for(i=1; i <= number_of_entry; i++){
 * if(version==1){
 * unsigned int(64) time;
 * unsigned int(64) moof_offset;
 * }else{
 * unsigned int(32) time;
 * unsigned int(32) moof_offset;
 * }
 * unsigned int((length_size_of_traf_num+1) * 8) traf_number;
 * unsigned int((length_size_of_trun_num+1) * 8) trun_number;
 * unsigned int((length_size_of_sample_num+1) * 8) sample_number;
 * }
 * }
 */
public class TrackFragmentRandomAccessBox extends AbstractFullBox {
    public static final String TYPE = "tfra";

    private long trackId;
    private int reserved;
    private int lengthSizeOfTrafNum;
    private int lengthSizeOfTrunNum;
    private int lengthSizeOfSampleNum;
    private List<Entry> entries = Collections.emptyList();

    public TrackFragmentRandomAccessBox() {
        super(TYPE);
    }


    protected long getContentSize() {
        long contentSize = 0;
        contentSize += 4 + 4 /*26 + 2 + 2 + 2 */ + 4;
        if (getVersion() == 1) {
            contentSize += (8 + 8) * entries.size();
        } else {
            contentSize += (4 + 4) * entries.size();
        }
        contentSize += (lengthSizeOfTrafNum + 1) * entries.size();
        contentSize += (lengthSizeOfTrunNum + 1) * entries.size();
        contentSize += (lengthSizeOfSampleNum + 1) * entries.size();
        return contentSize;
    }


    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        trackId = IsoTypeReader.readUInt32(content);
        long temp = IsoTypeReader.readUInt32(content);
        reserved = (int) (temp >> 6);
        lengthSizeOfTrafNum = (int) (temp & 0x3F) >> 4;
        lengthSizeOfTrunNum = (int) (temp & 0xC) >> 2;
        lengthSizeOfSampleNum = (int) (temp & 0x3);
        long numberOfEntries = IsoTypeReader.readUInt32(content);

        entries = new ArrayList<Entry>();

        for (int i = 0; i < numberOfEntries; i++) {
            Entry entry = new Entry();
            if (getVersion() == 1) {
                entry.time = IsoTypeReader.readUInt64(content);
                entry.moofOffset = IsoTypeReader.readUInt64(content);
            } else {
                entry.time = IsoTypeReader.readUInt32(content);
                entry.moofOffset = IsoTypeReader.readUInt32(content);
            }
            entry.trafNumber = getVariable(lengthSizeOfTrafNum, content);
            entry.trunNumber = getVariable(lengthSizeOfTrunNum, content);
            entry.sampleNumber = getVariable(lengthSizeOfSampleNum, content);

            entries.add(entry);
        }

    }

    private long getVariable(long length, ByteBuffer bb) {
        long ret;
        if (((length + 1) * 8) == 8) {
            ret = IsoTypeReader.readUInt8(bb);
        } else if (((length + 1) * 8) == 16) {
            ret = IsoTypeReader.readUInt16(bb);
        } else if (((length + 1) * 8) == 24) {
            ret = IsoTypeReader.readUInt24(bb);
        } else if (((length + 1) * 8) == 32) {
            ret = IsoTypeReader.readUInt32(bb);
        } else if (((length + 1) * 8) == 64) {
            ret = IsoTypeReader.readUInt64(bb);
        } else {
            throw new RuntimeException("lengthSize not power of two");
        }
        return ret;
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);
        IsoTypeWriter.writeUInt32(bb, trackId);
        long temp;
        temp = reserved << 6;
        temp = temp | ((lengthSizeOfTrafNum & 0x3) << 4);
        temp = temp | ((lengthSizeOfTrunNum & 0x3) << 2);
        temp = temp | (lengthSizeOfSampleNum & 0x3);
        IsoTypeWriter.writeUInt32(bb, temp);
        IsoTypeWriter.writeUInt32(bb, entries.size());

        for (Entry entry : entries) {
            if (getVersion() == 1) {
                IsoTypeWriter.writeUInt64(bb, entry.time);
                IsoTypeWriter.writeUInt64(bb, entry.moofOffset);
            } else {
                IsoTypeWriter.writeUInt32(bb, entry.time);
                IsoTypeWriter.writeUInt32(bb, entry.moofOffset);
            }
            bb.put(toByteArray(lengthSizeOfTrafNum + 1, entry.trafNumber));
            bb.put(toByteArray(lengthSizeOfTrunNum + 1, entry.trunNumber));
            bb.put(toByteArray(lengthSizeOfSampleNum + 1, entry.sampleNumber));
        }
    }

    private byte[] toByteArray(int length, long value) {
        byte[] b = new byte[length];
        int j = b.length;
        while (j > 0) {
            b[j - 1] = (byte) (value & 0xff);
            value = value >> 8;
            j--;
        }
        return b;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public void setLengthSizeOfTrafNum(int lengthSizeOfTrafNum) {
        this.lengthSizeOfTrafNum = lengthSizeOfTrafNum;
    }

    public void setLengthSizeOfTrunNum(int lengthSizeOfTrunNum) {
        this.lengthSizeOfTrunNum = lengthSizeOfTrunNum;
    }

    public void setLengthSizeOfSampleNum(int lengthSizeOfSampleNum) {
        this.lengthSizeOfSampleNum = lengthSizeOfSampleNum;
    }

    public long getTrackId() {
        return trackId;
    }

    public int getReserved() {
        return reserved;
    }

    public int getLengthSizeOfTrafNum() {
        return lengthSizeOfTrafNum;
    }

    public int getLengthSizeOfTrunNum() {
        return lengthSizeOfTrunNum;
    }

    public int getLengthSizeOfSampleNum() {
        return lengthSizeOfSampleNum;
    }

    public long getNumberOfEntries() {
        return entries.size();
    }

    public List<Entry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public static class Entry {
        private long time;
        private long moofOffset;
        private long trafNumber;
        private long trunNumber;
        private long sampleNumber;

        public long getTime() {
            return time;
        }

        public long getMoofOffset() {
            return moofOffset;
        }

        public long getTrafNumber() {
            return trafNumber;
        }

        public long getTrunNumber() {
            return trunNumber;
        }

        public long getSampleNumber() {
            return sampleNumber;
        }

        public void setTime(long time) {
            this.time = time;
        }

        public void setMoofOffset(long moofOffset) {
            this.moofOffset = moofOffset;
        }

        public void setTrafNumber(long trafNumber) {
            this.trafNumber = trafNumber;
        }

        public void setTrunNumber(long trunNumber) {
            this.trunNumber = trunNumber;
        }

        public void setSampleNumber(long sampleNumber) {
            this.sampleNumber = sampleNumber;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "time=" + time +
                    ", moofOffset=" + moofOffset +
                    ", trafNumber=" + trafNumber +
                    ", trunNumber=" + trunNumber +
                    ", sampleNumber=" + sampleNumber +
                    '}';
        }
    }

}
