/*
 * Copyright 2021 glboby27@gmail.com
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

package org.mp4parser.boxes.iso14496.part12;

import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <pre>
 * aligned(8) class SubsegmentIndexBox extends FullBox(‘ssix’, 0, 0) {
 *  unsigned int(32) subsegment_count;
 *  for( i=1; i <= subsegment_count; i++)
 *  {
 *   unsigned int(32)  ranges_count;
 *   for ( j=1; j <= range_count; j++) {
 *    unsigned int(8) level;
 *    unsigned int(24) range_size;
 *   }
 *  }
 * }
 * </pre>
 */
public class SubsegmentIndexBox extends AbstractFullBox {
    public static final String TYPE = "ssix";
    private long subsegmentCount;
    final private List<SubSegmentEntry> subSegEntries = new ArrayList<>();

    public SubsegmentIndexBox() { super(TYPE); }

    public long getSubsegmentCount() { return subsegmentCount; }

    public void setSubsegmentCount(long subsegmentCount) { this.subsegmentCount = subsegmentCount; }

    public List<SubSegmentEntry> getSubSegEntries() { return subSegEntries; }

    @Override
    protected long getContentSize() {
        long size = 4;
        for (SubSegmentEntry subSegEntry : subSegEntries) {
            size += 4;
            size += (subSegEntry.rangeEntries.size() * (long)(1 + 3));
        }
        return size;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        setSubsegmentCount(IsoTypeReader.readUInt32(content));
        for (int i = 0; i < getSubsegmentCount(); i++) {
            SubSegmentEntry subSegmentEntry = new SubSegmentEntry();
            subSegmentEntry.setRangesCount(IsoTypeReader.readUInt32(content));
            for (int j = 0; j < subSegmentEntry.getRangesCount(); j++) {
                SubSegmentEntry.RangeEntry rangeEntry = new SubSegmentEntry.RangeEntry();
                rangeEntry.setLevel(IsoTypeReader.readUInt8(content));
                rangeEntry.setRangeSize(IsoTypeReader.readUInt24(content));
                subSegmentEntry.getRangeEntries().add(rangeEntry);
            }
            getSubSegEntries().add(subSegmentEntry);
        }
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, getSubsegmentCount());
        for (SubSegmentEntry subSegmentEntry : getSubSegEntries()) {
            IsoTypeWriter.writeUInt32(byteBuffer, subSegmentEntry.getRangesCount());
            for (SubSegmentEntry.RangeEntry rangeEntry : subSegmentEntry.getRangeEntries()) {
                IsoTypeWriter.writeUInt8(byteBuffer, rangeEntry.getLevel());
                IsoTypeWriter.writeUInt24(byteBuffer, rangeEntry.getRangeSize());
            }
        }
    }

    public static class SubSegmentEntry {
        private long rangesCount;
        final private List<RangeEntry> rangeEntries = new ArrayList<>();

        public long getRangesCount() { return rangesCount; }

        public void setRangesCount(long rangesCount) { this.rangesCount = rangesCount; }

        public List<RangeEntry> getRangeEntries() { return rangeEntries; }

        public static class RangeEntry {
            private int level;
            private int rangeSize;

            public int getLevel() { return level; }

            public void setLevel(int level) { this.level = level; }

            public int getRangeSize() { return rangeSize; }

            public void setRangeSize(int rangeSize) { this.rangeSize = rangeSize; }
        }
    }
}
