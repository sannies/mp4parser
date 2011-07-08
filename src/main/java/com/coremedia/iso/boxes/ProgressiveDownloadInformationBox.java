package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ProgressiveDownloadInformationBox extends AbstractFullBox {


    List<Entry> entries = Collections.emptyList();

    public ProgressiveDownloadInformationBox() {
        super(IsoFile.fourCCtoBytes("pdin"));
    }

    @Override
    protected long getContentSize() {
        return entries.size() * 8;
    }

    @Override
    public String getDisplayName() {
        return "Progressive Download Info Box";
    }

    @Override
    protected void getContent(IsoOutputStream os) throws IOException {
        for (Entry entry : entries) {
            os.writeUInt32(entry.getRate());
            os.writeUInt32(entry.getInitialDelay());
        }
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
        size -= 4; // flags/version
        entries = new LinkedList<Entry>();
        while (size >= 8) {
            Entry entry = new Entry(in.readUInt32(), in.readUInt32());
            entries.add(entry);
            size -= 8;
        }

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
    }

    @Override
    public String toString() {
        return "ProgressiveDownloadInfoBox{" +
                "entries=" + entries +
                '}';
    }

}