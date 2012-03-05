package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.coremedia.iso.boxes.CastUtils.l2i;

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
        super(TYPE);
    }

    protected long getContentSize() {
        return 8 + 8 * entries.size();
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        int numberOfEntries = l2i(IsoTypeReader.readUInt32(content));
        entries = new ArrayList<Entry>(numberOfEntries);
        for (int i = 0; i < numberOfEntries; i++) {
            Entry e = new Entry(l2i(IsoTypeReader.readUInt32(content)), content.getInt());
            entries.add(e);
        }
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);
        IsoTypeWriter.writeUInt32(bb, entries.size());

        for (Entry entry : entries) {
            IsoTypeWriter.writeUInt32(bb, entry.getCount());
            bb.putInt(entry.getOffset());
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

}
