package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;
import java.util.*;

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

    public String getDisplayName() {
        return "Composition Time to Sample Box";
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
            Entry e = new Entry(in.readUInt32(),in.readInt32());
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
        long count;
        int offset;

        public Entry(long count, int offset) {
            this.count = count;
            this.offset = offset;
        }

        public long getCount() {
            return count;
        }

        public int getOffset() {
            return offset;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "count=" + count +
                    ", offset=" + offset +
                    '}';
        }
    }
}
