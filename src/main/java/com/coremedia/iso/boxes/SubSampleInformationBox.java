package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * aligned(8) class SubSampleInformationBox
 * extends FullBox('subs', version, 0) {
 * unsigned int(32) entry_count;
 * int i,j;
 * for (i=0; i < entry_count; i++) {
 * unsigned int(32) sample_delta;
 * unsigned int(16) subsample_count;
 * if (subsample_count > 0) {
 * for (j=0; j < subsample_count; j++) {
 * if(version == 1)
 * {
 * unsigned int(32) subsample_size;
 * }
 * else
 * {
 * unsigned int(16) subsample_size;
 * }
 * unsigned int(8) subsample_priority;
 * unsigned int(8) discardable;
 * unsigned int(32) reserved = 0;
 * }
 * }
 * }
 * }
 */
public class SubSampleInformationBox extends AbstractFullBox {
    public static final String TYPE = "subs";

    private long entryCount;
    private List<SampleEntry> sampleEntries = new ArrayList<SampleEntry>();

    public SubSampleInformationBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    @Override
    protected long getContentSize() {
        long entries = 4 + ((4 + 2) * entryCount);
        int subsampleEntries = 0;
        for (SampleEntry sampleEntry : sampleEntries) {
            subsampleEntries += sampleEntry.getSubsampleCount() * (((getVersion() == 1) ? 4 : 2) + 1 + 1 + 4);
        }
        return entries + subsampleEntries;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);

        entryCount = in.readUInt32();

        for (int i = 0; i < entryCount; i++) {
            SampleEntry sampleEntry = new SampleEntry();
            sampleEntry.setSampleDelta(in.readUInt32());
            int subsampleCount = in.readUInt16();
            sampleEntry.setSubsampleCount(subsampleCount);
            for (int j = 0; j < subsampleCount; j++) {
                SampleEntry.SubsampleEntry subsampleEntry = new SampleEntry.SubsampleEntry();
                subsampleEntry.setSubsampleSize(getVersion() == 1 ? in.readUInt32() : in.readUInt16());
                subsampleEntry.setSubsamplePriority(in.readUInt8());
                subsampleEntry.setDiscardable(in.readUInt8());
                subsampleEntry.setReserved(in.readUInt32());
                sampleEntry.addSubsampleEntry(subsampleEntry);
            }
            sampleEntries.add(sampleEntry);
        }

    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt32(entryCount);
        for (SampleEntry sampleEntry : sampleEntries) {
            os.writeUInt32(sampleEntry.getSampleDelta());
            List<SampleEntry.SubsampleEntry> subsampleEntries = sampleEntry.getSubsampleEntries();
            for (SampleEntry.SubsampleEntry subsampleEntry : subsampleEntries) {
                if (getVersion() == 1) {
                    os.writeUInt32(subsampleEntry.getSubsampleSize());
                } else {
                    os.writeUInt16((int) subsampleEntry.getSubsampleSize());
                }
                os.writeUInt8(subsampleEntry.getSubsamplePriority());
                os.writeUInt8(subsampleEntry.getDiscardable());
                os.writeUInt32(subsampleEntry.getReserved());
            }
        }
    }

    public static class SampleEntry {
        private long sampleDelta;
        private int subsampleCount;
        private List<SubsampleEntry> subsampleEntries = new ArrayList<SubsampleEntry>();

        public long getSampleDelta() {
            return sampleDelta;
        }

        public void setSampleDelta(long sampleDelta) {
            this.sampleDelta = sampleDelta;
        }

        public int getSubsampleCount() {
            return subsampleCount;
        }

        public void setSubsampleCount(int subsampleCount) {
            this.subsampleCount = subsampleCount;
        }

        public List<SubsampleEntry> getSubsampleEntries() {
            return subsampleEntries;
        }

        public void addSubsampleEntry(SubsampleEntry subsampleEntry) {
            subsampleEntries.add(subsampleEntry);
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
        }
    }
}
