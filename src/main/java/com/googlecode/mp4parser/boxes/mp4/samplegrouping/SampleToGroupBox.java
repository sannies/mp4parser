package com.googlecode.mp4parser.boxes.mp4.samplegrouping;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.FullBox;
import com.googlecode.mp4parser.AbstractFullBox;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * This table can be used to find the group that a sample belongs to and the associated description of that
 * sample group. The table is compactly coded with each entry giving the index of the first sample of a run of
 * samples with the same sample group descriptor. The sample group description ID is an index that refers to a
 * SampleGroupDescription box, which contains entries describing the characteristics of each sample group.
 * <p/>
 * There may be multiple instances of this box if there is more than one sample grouping for the samples in a
 * track. Each instance of the SampleToGroup box has a type code that distinguishes different sample
 * groupings. Within a track, there shall be at most one instance of this box with a particular grouping type. The
 * associated SampleGroupDescription shall indicate the same value for the grouping type.
 * <p/>
 * Version 1 of this box should only be used if a grouping type parameter is needed.
 */
public class SampleToGroupBox extends AbstractFullBox {
    public static final String TYPE = "sbgp";


    String groupingType;
    String groupingTypeParameter;

    List<Entry> entries = new LinkedList<Entry>();

    public SampleToGroupBox() {
        super(TYPE);

    }

    @Override
    protected long getContentSize() {
        return this.getVersion() == 1 ? entries.size() * 8 + 16 : entries.size() * 8 + 12;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(groupingType.getBytes());
        if (this.getVersion() == 1) {
            byteBuffer.put(groupingTypeParameter.getBytes());
        }
        IsoTypeWriter.writeUInt32(byteBuffer, entries.size());
        for (Entry entry : entries) {
            IsoTypeWriter.writeUInt32(byteBuffer, entry.getSampleCount());
            IsoTypeWriter.writeUInt32(byteBuffer, entry.getGroupDescriptionIndex());
        }

    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        groupingType = IsoTypeReader.read4cc(content);
        if (this.getVersion() == 1) {
            groupingTypeParameter = IsoTypeReader.read4cc(content);
        }
        long entryCount = IsoTypeReader.readUInt32(content);
        while (entryCount-- > 0) {
            entries.add(new Entry(l2i(IsoTypeReader.readUInt32(content)), l2i(IsoTypeReader.readUInt32(content))));
        }
    }

    public static class Entry {
        private long sampleCount;
        private int groupDescriptionIndex;

        public Entry(long sampleCount, int groupDescriptionIndex) {
            this.sampleCount = sampleCount;
            this.groupDescriptionIndex = groupDescriptionIndex;
        }

        public long getSampleCount() {
            return sampleCount;
        }

        public void setSampleCount(long sampleCount) {
            this.sampleCount = sampleCount;
        }

        public int getGroupDescriptionIndex() {
            return groupDescriptionIndex;
        }

        public void setGroupDescriptionIndex(int groupDescriptionIndex) {
            this.groupDescriptionIndex = groupDescriptionIndex;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "sampleCount=" + sampleCount +
                    ", groupDescriptionIndex=" + groupDescriptionIndex +
                    '}';
        }
    }

    public String getGroupingType() {
        return groupingType;
    }

    public void setGroupingType(String groupingType) {
        this.groupingType = groupingType;
    }

    public String getGroupingTypeParameter() {
        return groupingTypeParameter;
    }

    public void setGroupingTypeParameter(String groupingTypeParameter) {
        this.groupingTypeParameter = groupingTypeParameter;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }
}
