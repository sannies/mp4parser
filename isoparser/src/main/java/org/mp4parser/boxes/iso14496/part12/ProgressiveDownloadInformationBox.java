package org.mp4parser.boxes.iso14496.part12;

import org.mp4parser.support.AbstractFullBox;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public class ProgressiveDownloadInformationBox extends AbstractFullBox {
    public static final String TYPE = "pdin";

    List<Entry> entries = Collections.emptyList();

    public ProgressiveDownloadInformationBox() {
        super(TYPE);
    }

    @Override
    protected long getContentSize() {
        return 4 + entries.size() * 8;
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        for (Entry entry : entries) {
            IsoTypeWriter.writeUInt32(byteBuffer, entry.getRate());
            IsoTypeWriter.writeUInt32(byteBuffer, entry.getInitialDelay());
        }
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
        entries = new LinkedList<Entry>();
        while (content.remaining() >= 8) {
            Entry entry = new Entry(IsoTypeReader.readUInt32(content), IsoTypeReader.readUInt32(content));
            entries.add(entry);
        }
    }

    @Override
    public String toString() {
        return "ProgressiveDownloadInfoBox{" +
                "entries=" + entries +
                '}';
    }

    public static class Entry {
        long rate;
        long initialDelay;

        public Entry(long rate, long initialDelay) {
            this.rate = rate;
            this.initialDelay = initialDelay;
        }

        public long getRate() {
            return rate;
        }

        public void setRate(long rate) {
            this.rate = rate;
        }

        public long getInitialDelay() {
            return initialDelay;
        }

        public void setInitialDelay(long initialDelay) {
            this.initialDelay = initialDelay;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "rate=" + rate +
                    ", initialDelay=" + initialDelay +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Entry entry = (Entry) o;

            if (initialDelay != entry.initialDelay) return false;
            if (rate != entry.rate) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (rate ^ (rate >>> 32));
            result = 31 * result + (int) (initialDelay ^ (initialDelay >>> 32));
            return result;
        }
    }

}