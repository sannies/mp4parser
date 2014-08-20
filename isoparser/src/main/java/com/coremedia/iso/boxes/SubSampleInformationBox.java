package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <pre>
 * aligned(8) class SubSampleInformationBox extends FullBox('subs', version, 0) {
 *  unsigned int(32) entry_count;
 *  int i,j;
 *  for (i=0; i &lt; entry_count; i++) {
 *   unsigned int(32) sample_delta;
 *   unsigned int(16) subsample_count;
 *   if (subsample_count &gt; 0) {
 *    for (j=0; j &lt; subsample_count; j++) {
 *     if(version == 1)
 *     {
 *      unsigned int(32) subsample_size;
 *     }
 *     else
 *     {
 *      unsigned int(16) subsample_size;
 *     }
 *     unsigned int(8) subsample_priority;
 *     unsigned int(8) discardable;
 *     unsigned int(32) reserved = 0;
 *    }
 *   }
 *  }
 * }
 * </pre>
 */
public class SubSampleInformationBox extends AbstractFullBox {
    public static final String TYPE = "subs";

    private List<SubSampleEntry> entries = new ArrayList<SubSampleEntry>();

    public SubSampleInformationBox() {
        super(TYPE);
    }

    public List<SubSampleEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<SubSampleEntry> entries) {
        this.entries = entries;
    }

    @Override
    protected long getContentSize() {
        long size = 8;

        for (SubSampleEntry entry : entries) {
            size += 4;
            size += 2;
            for (int j = 0; j < entry.getSubsampleEntries().size(); j++) {

                if (getVersion() == 1) {
                    size += 4;
                } else {
                    size += 2;
                }
                size += 2;
                size += 4;
            }
        }
        return size;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);

        long entryCount = IsoTypeReader.readUInt32(content);

        for (int i = 0; i < entryCount; i++) {
            SubSampleEntry SubSampleEntry = new SubSampleEntry();
            SubSampleEntry.setSampleDelta(IsoTypeReader.readUInt32(content));
            int subsampleCount = IsoTypeReader.readUInt16(content);
            for (int j = 0; j < subsampleCount; j++) {
                SubSampleEntry.SubsampleEntry subsampleEntry = new SubSampleEntry.SubsampleEntry();
                subsampleEntry.setSubsampleSize(getVersion() == 1 ? IsoTypeReader.readUInt32(content) : IsoTypeReader.readUInt16(content));
                subsampleEntry.setSubsamplePriority(IsoTypeReader.readUInt8(content));
                subsampleEntry.setDiscardable(IsoTypeReader.readUInt8(content));
                subsampleEntry.setReserved(IsoTypeReader.readUInt32(content));
                SubSampleEntry.getSubsampleEntries().add(subsampleEntry);
            }
            entries.add(SubSampleEntry);
        }

    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, entries.size());
        for (SubSampleEntry subSampleEntry : entries) {
            IsoTypeWriter.writeUInt32(byteBuffer, subSampleEntry.getSampleDelta());
            IsoTypeWriter.writeUInt16(byteBuffer, subSampleEntry.getSubsampleCount());
            List<SubSampleEntry.SubsampleEntry> subsampleEntries = subSampleEntry.getSubsampleEntries();
            for (SubSampleEntry.SubsampleEntry subsampleEntry : subsampleEntries) {
                if (getVersion() == 1) {
                    IsoTypeWriter.writeUInt32(byteBuffer, subsampleEntry.getSubsampleSize());
                } else {
                    IsoTypeWriter.writeUInt16(byteBuffer, l2i(subsampleEntry.getSubsampleSize()));
                }
                IsoTypeWriter.writeUInt8(byteBuffer, subsampleEntry.getSubsamplePriority());
                IsoTypeWriter.writeUInt8(byteBuffer, subsampleEntry.getDiscardable());
                IsoTypeWriter.writeUInt32(byteBuffer, subsampleEntry.getReserved());
            }
        }
    }

    @Override
    public String toString() {
        return "SubSampleInformationBox{" +
                "entryCount=" + entries.size() +
                ", entries=" + entries +
                '}';
    }

    public static class SubSampleEntry {
        private long sampleDelta;
        private List<SubsampleEntry> subsampleEntries = new ArrayList<SubsampleEntry>();

        public long getSampleDelta() {
            return sampleDelta;
        }

        public void setSampleDelta(long sampleDelta) {
            this.sampleDelta = sampleDelta;
        }

        public int getSubsampleCount() {
            return subsampleEntries.size();
        }

        public List<SubsampleEntry> getSubsampleEntries() {
            return subsampleEntries;
        }

        public static class SubsampleEntry {
            private long subsampleSize;
            private int subsamplePriority;
            private int discardable;
            private long reserved;

            public long getSubsampleSize() {
                return subsampleSize;
            }

            public void setSubsampleSize(long subsampleSize) {
                this.subsampleSize = subsampleSize;
            }

            public int getSubsamplePriority() {
                return subsamplePriority;
            }

            public void setSubsamplePriority(int subsamplePriority) {
                this.subsamplePriority = subsamplePriority;
            }

            public int getDiscardable() {
                return discardable;
            }

            public void setDiscardable(int discardable) {
                this.discardable = discardable;
            }

            public long getReserved() {
                return reserved;
            }

            public void setReserved(long reserved) {
                this.reserved = reserved;
            }

            @Override
            public String toString() {
                return "SubsampleEntry{" +
                        "subsampleSize=" + subsampleSize +
                        ", subsamplePriority=" + subsamplePriority +
                        ", discardable=" + discardable +
                        ", reserved=" + reserved +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "SampleEntry{" +
                    "sampleDelta=" + sampleDelta +
                    ", subsampleCount=" + subsampleEntries.size() +
                    ", subsampleEntries=" + subsampleEntries +
                    '}';
        }
    }
}
