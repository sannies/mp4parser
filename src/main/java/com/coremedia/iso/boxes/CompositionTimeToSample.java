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
 * This box provides the offset between decoding time and composition time.
 * Since decoding time must be less than the composition time, the offsets
 * are expressed as unsigned numbers such that CT(n) = DT(n) + CTTS(n) where
 * CTTS(n) is the (uncompressed) table entry for sample n. The composition
 * time to sample table is optional and must only be present if DT and CT
 * differ for any samples. Hint tracks do not use this box.
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
