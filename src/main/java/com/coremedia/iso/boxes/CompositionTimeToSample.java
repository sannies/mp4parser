package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * aligned(8) class CompositionOffsetBox
extends FullBox(‘ctts’, version = 0, 0) {
unsigned int(32) entry_count;
int i;
if (version==0) {
for (i=0; i < entry_count; i++) {
unsigned int(32) sample_count;
unsigned int(32) sample_offset;
}
}
else if (version == 1) {
for (i=0; i < entry_count; i++) {
unsigned int(32) sample_count;
signed int(32) sample_offset;
}
}
}
 *
 * This box provides the offset between decoding time and composition time.
 * In version 0 of this box the decoding time must be less than the composition time, and
 * the offsets are expressed as unsigned numbers such that
 * CT(n) = DT(n) + CTTS(n) where CTTS(n) is the (uncompressed) table entry for sample n.
 *
 * In version 1 of this box, the composition timeline and the decoding timeline are
 * still derived from each other, but the offsets are signed.
 * It is recommended that for the computed composition timestamps, there is
 * exactly one with the value 0 (zero).
 * 
 */
public class CompositionTimeToSample extends AbstractFullBox {
    public static final String TYPE = "ctts";

    List<Entry> entries = Collections.emptyList();

    public CompositionTimeToSample() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    protected long getContentSize() {
        return 4 + 8 * entries.size();
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        long numberOfEntries = in.readUInt32();
        assert numberOfEntries <= Integer.MAX_VALUE : "Too many entries";
        entries = new ArrayList<Entry>((int) numberOfEntries);
        for (int i = 0; i < numberOfEntries; i++) {
            Entry e = new Entry(toint(in.readUInt32()), toint(in.readInt32()));
            entries.add(e);
        }
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt32(entries.size());

        for (Entry entry : entries) {
            os.writeUInt32(entry.getCount());
            os.writeInt32(entry.getOffset());
        }
    }

    public static class Entry {
        int count;
        int offset;

        public Entry(int count, int offset) {
            this.count = count;
            this.offset = offset;
        }

        public int getCount() {
            return count;
        }

        public int getOffset() {
            return offset;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void setOffset(int offset) {
            this.offset = offset;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "count=" + count +
                    ", offset=" + offset +
                    '}';
        }
    }


    /**
     * Decompresses the list of entries and returns the list of composition times.
     *
     * @return decoding time per sample
     */
    public static int[] blowupCompositionTimes(List<CompositionTimeToSample.Entry> entries) {
        long numOfSamples = 0;
        for (CompositionTimeToSample.Entry entry : entries) {
            numOfSamples += entry.getCount();
        }
        assert numOfSamples <= Integer.MAX_VALUE;
        int[] decodingTime = new int[(int) numOfSamples];

        int current = 0;


        for (CompositionTimeToSample.Entry entry : entries) {
            for (int i = 0; i < entry.getCount(); i++) {
                decodingTime[current++] = entry.getOffset();
            }
        }

        return decodingTime;
    }
    
    public static int toint(long l)  {
        if (l>Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Event though I didn't expect it the given long is greater than Integer.MAX_VALUE");
        } else {
            return (int) l;
        }
    }
}
