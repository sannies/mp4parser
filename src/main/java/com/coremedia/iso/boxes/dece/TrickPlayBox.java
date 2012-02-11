package com.coremedia.iso.boxes.dece;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractFullBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * aligned(8) class TrickPlayBox
 * extends FullBox(‘trik’, version=0, flags=0)
 * {
 * for (i=0; I < sample_count; i++) {
 * unsigned int(2) pic_type;
 * unsigned int(6) dependency_level;
 * }
 * }
 */
public class TrickPlayBox extends AbstractFullBox {
    public static final String TYPE = "trik";

    private List<Entry> entries = new ArrayList<Entry>();

    public TrickPlayBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public static class Entry {

        public Entry(int value) {
            this.value = value;
        }

        public Entry() {
        }

        private int value;

        public int getPicType() {
            return (value >> 6) & 0x03;
        }

        public void setPicType(int picType) {
            value = (picType & 0x03) << 6 | value;
        }

        public int getDependencyLevel() {
            return value & 0x3f;
        }

        public void setDependencyLevel(int dependencyLevel) {
            value = (dependencyLevel & 0x3f) | value;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder();
            sb.append("Entry");
            sb.append("{picType=").append(getPicType());
            sb.append(",dependencyLevel=").append(getDependencyLevel());
            sb.append('}');
            return sb.toString();
        }
    }

    @Override
    protected long getContentSize() {
        return entries.size();
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        for (Entry entry : entries) {
            os.write(entry.value);
        }
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        long remainingBytes = size - 4;

        while (remainingBytes > 0) {
            entries.add(new Entry(in.readUInt8()));
            remainingBytes--;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("TrickPlayBox");
        sb.append("{entries=").append(entries);
        sb.append('}');
        return sb.toString();
    }
}
