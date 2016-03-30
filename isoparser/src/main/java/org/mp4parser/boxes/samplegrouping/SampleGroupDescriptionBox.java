/*
 * Copyright 2012 castLabs, Berlin
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

package org.mp4parser.boxes.samplegrouping;

import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part15.StepwiseTemporalLayerEntry;
import org.mp4parser.boxes.iso14496.part15.SyncSampleEntry;
import org.mp4parser.boxes.iso14496.part15.TemporalLayerSampleGroup;
import org.mp4parser.boxes.iso14496.part15.TemporalSubLayerSampleGroup;
import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.tools.CastUtils;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <p>This description table gives information about the characteristics of sample groups. The descriptive
 * information is any other information needed to define or characterize the sample group.</p>
 * <p>There may be multiple instances of this box if there is more than one sample grouping for the samples in a
 * track. Each instance of the SampleGroupDescription box has a type code that distinguishes different
 * sample groupings. Within a track, there shall be at most one instance of this box with a particular grouping
 * type. The associated SampleToGroup shall indicate the same value for the grouping type.</p>
 * <p>The information is stored in the sample group description box after the entry-count. An abstract entry type is
 * defined and sample groupings shall define derived types to represent the description of each sample group.
 * For video tracks, an abstract VisualSampleGroupEntry is used with similar types for audio and hint tracks.</p>
 */
public class SampleGroupDescriptionBox extends AbstractFullBox {
    public static final String TYPE = "sgpd";
    private String groupingType;
    private int defaultLength;
    private List<GroupEntry> groupEntries = new LinkedList<GroupEntry>();

    public SampleGroupDescriptionBox() {
        super(TYPE);
        setVersion(1);
    }

    public String getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(String groupingType) {
        this.groupingType = groupingType;
    }

    @Override
    protected long getContentSize() {
        long size = 8;
        if (getVersion() == 1) {
            size += 4;
        }
        size += 4; // entryCount
        for (GroupEntry groupEntry : groupEntries) {
            if (getVersion() == 1 && defaultLength == 0) {
                size += 4;
            }
            size += defaultLength == 0 ? groupEntry.size() : defaultLength;
        }
        return size;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(IsoFile.fourCCtoBytes(groupingType));
        if (this.getVersion() == 1) {
            IsoTypeWriter.writeUInt32(byteBuffer, defaultLength);
        }
        IsoTypeWriter.writeUInt32(byteBuffer, this.groupEntries.size());
        for (GroupEntry entry : groupEntries) {
            ByteBuffer data = entry.get();
            if (this.getVersion() == 1) {
                if (defaultLength == 0) {
                    IsoTypeWriter.writeUInt32(byteBuffer, data.limit());
                } else {
                    if (data.limit() > defaultLength) {
                        throw new RuntimeException(
                                String.format("SampleGroupDescriptionBox entry size %d more than %d", data.limit(), defaultLength));
                    }
                }
            }
            byteBuffer.put(data);

            int deadBytes = defaultLength == 0 ? 0 : defaultLength - data.limit();
            while (deadBytes-- > 0) {
                byteBuffer.put((byte) 0);
            }
        }
    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        groupingType = IsoTypeReader.read4cc(content);
        if (this.getVersion() == 1) {
            defaultLength = CastUtils.l2i(IsoTypeReader.readUInt32(content));
        }
        long entryCount = IsoTypeReader.readUInt32(content);
        while (entryCount-- > 0) {
            int length = defaultLength;
            if (this.getVersion() == 1) {
                if (defaultLength == 0) {
                    length = CastUtils.l2i(IsoTypeReader.readUInt32(content));
                }
            } else {
                length = content.limit() - content.position(); 
            }
            ByteBuffer parseMe = content.slice();
            parseMe.limit(length);
            groupEntries.add(parseGroupEntry(parseMe, groupingType));
            int parsedBytes = this.getVersion() == 1 ? length : parseMe.position(); 
            content.position(content.position() + parsedBytes);
        }

    }

    private GroupEntry parseGroupEntry(ByteBuffer content, String groupingType) {
        GroupEntry groupEntry;
        if (RollRecoveryEntry.TYPE.equals(groupingType)) {
            groupEntry = new RollRecoveryEntry();
        } else if (RateShareEntry.TYPE.equals(groupingType)) {
            groupEntry = new RateShareEntry();
        } else if (CencSampleEncryptionInformationGroupEntry.TYPE.equals(groupingType)) {
            groupEntry = new CencSampleEncryptionInformationGroupEntry();
        } else if (VisualRandomAccessEntry.TYPE.equals(groupingType)) {
            groupEntry = new VisualRandomAccessEntry();
        } else if (TemporalLevelEntry.TYPE.equals(groupingType)) {
            groupEntry = new TemporalLevelEntry();
        } else if (SyncSampleEntry.TYPE.equals(groupingType)) {
            groupEntry = new SyncSampleEntry();
        } else if (TemporalLayerSampleGroup.TYPE.equals(groupingType)) {
            groupEntry = new TemporalLayerSampleGroup();
        } else if (TemporalSubLayerSampleGroup.TYPE.equals(groupingType)) {
            groupEntry = new TemporalSubLayerSampleGroup();
        } else if (StepwiseTemporalLayerEntry.TYPE.equals(groupingType)) {
            groupEntry = new StepwiseTemporalLayerEntry();
        } else {
            if (this.getVersion() == 0) {
                throw new RuntimeException("SampleGroupDescriptionBox with UnknownEntry are only supported in version 1");                
            }
            groupEntry = new UnknownEntry(groupingType);
        }
        groupEntry.parse(content);
        return groupEntry;
    }

    public int getDefaultLength() {
        return defaultLength;
    }

    public void setDefaultLength(int defaultLength) {
        this.defaultLength = defaultLength;
    }

    public List<GroupEntry> getGroupEntries() {
        return groupEntries;
    }

    public void setGroupEntries(List<GroupEntry> groupEntries) {
        this.groupEntries = groupEntries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SampleGroupDescriptionBox that = (SampleGroupDescriptionBox) o;

        if (defaultLength != that.defaultLength) {
            return false;
        }
        if (groupEntries != null ? !groupEntries.equals(that.groupEntries) : that.groupEntries != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = 0;
        result = 31 * result + defaultLength;
        result = 31 * result + (groupEntries != null ? groupEntries.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SampleGroupDescriptionBox{" +
                "groupingType='" + (groupEntries.size() > 0 ? groupEntries.get(0).getType() : "????") + '\'' +
                ", defaultLength=" + defaultLength +
                ", groupEntries=" + groupEntries +
                '}';
    }
}
