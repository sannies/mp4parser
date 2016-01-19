package org.mp4parser.boxes.iso14496.part12;

import org.mp4parser.boxes.iso14496.part1.objectdescriptors.BitReaderBuffer;
import org.mp4parser.boxes.iso14496.part1.objectdescriptors.BitWriterBuffer;
import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <pre>
 * aligned(8) class SegmentIndexBox extends FullBox(‘sidx’, version, 0) {
 *  unsigned int(32) reference_ID;
 *  unsigned int(32) timescale;
 *  if (version==0)
 *  {
 *   unsigned int(32) earliest_presentation_time;
 *   unsigned int(32) first_offset;
 *  }
 *  else
 *  {
 *   unsigned int(64) earliest_presentation_time;
 *   unsigned int(64) first_offset;
 *  }
 *  unsigned int(16) reserved = 0;
 *  unsigned int(16) reference_count;
 *  for(i=1; i &lt;= reference_count; i++)
 *  {
 *   bit (1)            reference_type;
 *   unsigned int(31)   referenced_size;
 *   unsigned int(32)   subsegment_duration;
 *   bit(1)             starts_with_SAP;
 *   unsigned int(3)    SAP_type;
 *   unsigned int(28)   SAP_delta_time;
 *  }
 * }
 * </pre>
 */
public class SegmentIndexBox extends AbstractFullBox {
    public static final String TYPE = "sidx";
    List<Entry> entries = new ArrayList<Entry>();

    long referenceId;
    long timeScale;
    long earliestPresentationTime;
    long firstOffset;
    int reserved;


    public SegmentIndexBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        long size = 4;
        size += 4;
        size += 4;
        size += getVersion() == 0 ? 8 : 16;
        size += 2; // reserved
        size += 2; // reference count

        size += entries.size() * 12;

        return size;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, referenceId);
        IsoTypeWriter.writeUInt32(byteBuffer, timeScale);
        if (getVersion() == 0) {
            IsoTypeWriter.writeUInt32(byteBuffer, earliestPresentationTime);
            IsoTypeWriter.writeUInt32(byteBuffer, firstOffset);
        } else {
            IsoTypeWriter.writeUInt64(byteBuffer, earliestPresentationTime);
            IsoTypeWriter.writeUInt64(byteBuffer, firstOffset);
        }
        IsoTypeWriter.writeUInt16(byteBuffer, reserved);
        IsoTypeWriter.writeUInt16(byteBuffer, entries.size());
        for (Entry entry : entries) {
            BitWriterBuffer b = new BitWriterBuffer(byteBuffer);
            b.writeBits(entry.getReferenceType(), 1);
            b.writeBits(entry.getReferencedSize(), 31);
            IsoTypeWriter.writeUInt32(byteBuffer, entry.getSubsegmentDuration());
            b = new BitWriterBuffer(byteBuffer);
            b.writeBits(entry.getStartsWithSap(), 1);
            b.writeBits(entry.getSapType(), 3);
            b.writeBits(entry.getSapDeltaTime(), 28);
        }

    }

    @Override
    protected void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        referenceId = IsoTypeReader.readUInt32(content);
        timeScale = IsoTypeReader.readUInt32(content);
        if (getVersion() == 0) {
            earliestPresentationTime = IsoTypeReader.readUInt32(content);
            firstOffset = IsoTypeReader.readUInt32(content);
        } else {
            earliestPresentationTime = IsoTypeReader.readUInt64(content);
            firstOffset = IsoTypeReader.readUInt64(content);
        }
        reserved = IsoTypeReader.readUInt16(content);
        int numEntries = IsoTypeReader.readUInt16(content);
        for (int i = 0; i < numEntries; i++) {
            BitReaderBuffer b = new BitReaderBuffer(content);
            Entry e = new Entry();
            e.setReferenceType((byte) b.readBits(1));
            e.setReferencedSize(b.readBits(31));
            e.setSubsegmentDuration(IsoTypeReader.readUInt32(content));
            b = new BitReaderBuffer(content);
            e.setStartsWithSap((byte) b.readBits(1));
            e.setSapType((byte) b.readBits(3));
            e.setSapDeltaTime(b.readBits(28));
            entries.add(e);
        }
    }


    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public long getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(long referenceId) {
        this.referenceId = referenceId;
    }

    public long getTimeScale() {
        return timeScale;
    }

    public void setTimeScale(long timeScale) {
        this.timeScale = timeScale;
    }

    public long getEarliestPresentationTime() {
        return earliestPresentationTime;
    }

    public void setEarliestPresentationTime(long earliestPresentationTime) {
        this.earliestPresentationTime = earliestPresentationTime;
    }

    public long getFirstOffset() {
        return firstOffset;
    }

    public void setFirstOffset(long firstOffset) {
        this.firstOffset = firstOffset;
    }

    public int getReserved() {
        return reserved;
    }

    public void setReserved(int reserved) {
        this.reserved = reserved;
    }

    @Override
    public String toString() {
        return "SegmentIndexBox{" +
                "entries=" + entries +
                ", referenceId=" + referenceId +
                ", timeScale=" + timeScale +
                ", earliestPresentationTime=" + earliestPresentationTime +
                ", firstOffset=" + firstOffset +
                ", reserved=" + reserved +
                '}';
    }

    public static class Entry {
        byte referenceType;
        int referencedSize;
        long subsegmentDuration;
        byte startsWithSap;
        byte sapType;
        int sapDeltaTime;

        public Entry() {
        }

        public Entry(int referenceType, int referencedSize, long subsegmentDuration, boolean startsWithSap, int sapType, int sapDeltaTime) {
            this.referenceType = (byte) referenceType;
            this.referencedSize = referencedSize;
            this.subsegmentDuration = subsegmentDuration;
            this.startsWithSap = (byte) (startsWithSap ? 1 : 0);
            this.sapType = (byte) sapType;
            this.sapDeltaTime = sapDeltaTime;
        }

        /**
         * When set to 1 indicates that the reference is to a segment index ('sidx') box;
         * otherwise the reference is to media content (e.g., in the case of files based on this specification, to a
         * movie fragment box); if a separate index segment is used, then entries with reference t
         *
         * @return the reference type
         */
        public byte getReferenceType() {
            return referenceType;
        }

        /**
         * When set to 1 indicates that the reference is to a segment index ('sidx') box;
         * otherwise the reference is to media content (e.g., in the case of files based on this specification, to a
         * movie fragment box); if a separate index segment is used, then entries with reference t
         *
         * @param referenceType the new reference type
         */
        public void setReferenceType(byte referenceType) {
            this.referenceType = referenceType;
        }

        public int getReferencedSize() {
            return referencedSize;
        }

        public void setReferencedSize(int referencedSize) {
            this.referencedSize = referencedSize;
        }

        public long getSubsegmentDuration() {
            return subsegmentDuration;
        }

        public void setSubsegmentDuration(long subsegmentDuration) {
            this.subsegmentDuration = subsegmentDuration;
        }

        public byte getStartsWithSap() {
            return startsWithSap;
        }

        public void setStartsWithSap(byte startsWithSap) {
            this.startsWithSap = startsWithSap;
        }

        public byte getSapType() {
            return sapType;
        }

        public void setSapType(byte sapType) {
            this.sapType = sapType;
        }

        public int getSapDeltaTime() {
            return sapDeltaTime;
        }

        public void setSapDeltaTime(int sapDeltaTime) {
            this.sapDeltaTime = sapDeltaTime;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "referenceType=" + referenceType +
                    ", referencedSize=" + referencedSize +
                    ", subsegmentDuration=" + subsegmentDuration +
                    ", startsWithSap=" + startsWithSap +
                    ", sapType=" + sapType +
                    ", sapDeltaTime=" + sapDeltaTime +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (referenceType != entry.referenceType) return false;
            if (referencedSize != entry.referencedSize) return false;
            if (sapDeltaTime != entry.sapDeltaTime) return false;
            if (sapType != entry.sapType) return false;
            if (startsWithSap != entry.startsWithSap) return false;
            if (subsegmentDuration != entry.subsegmentDuration) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) referenceType;
            result = 31 * result + referencedSize;
            result = 31 * result + (int) (subsegmentDuration ^ (subsegmentDuration >>> 32));
            result = 31 * result + (int) startsWithSap;
            result = 31 * result + (int) sapType;
            result = 31 * result + sapDeltaTime;
            return result;
        }
    }
}
