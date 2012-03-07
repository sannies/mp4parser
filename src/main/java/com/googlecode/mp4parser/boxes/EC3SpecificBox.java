package com.googlecode.mp4parser.boxes;

import com.coremedia.iso.boxes.AbstractBox;
import com.googlecode.mp4parser.boxes.mp4.objectdescriptors.BitReaderBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 3/7/12
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class EC3SpecificBox extends AbstractBox {
    List<Entry> entries = new LinkedList<Entry>();
    int data_rate;
    int numIndSub;

    public EC3SpecificBox() {
        super("dec3");
    }

    @Override
    protected long getContentSize() {
        long size = 2;
        for (Entry entry : entries) {
            if (entry.num_dep_sub >0) {
                size += 4;
            } else {
                size += 3;
            }
        }
        return size;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        BitReaderBuffer brb = new BitReaderBuffer(content);
        data_rate = brb.readBits(13);
        numIndSub = brb.readBits(3) + 1;
        // This field indicates the number of independent substreams that are present in the Enhanced AC-3 bitstream. The value
        // of this field is one less than the number of independent substreams present.


        for (int i = 0; i < numIndSub; i++) {
            Entry e = new Entry();
            e.fscod = brb.readBits(2);
            e.bsid = brb.readBits(5);
            e.bsmod = brb.readBits(5);
            e.acmod = brb.readBits(3);
            e.lfeon = brb.readBits(1);
            e.reserved = brb.readBits(3);
            e.num_dep_sub = brb.readBits(4);
            if (e.num_dep_sub > 0) {
                e.chan_loc = brb.readBits(9);
            } else {
                e.reserved2 = brb.readBits(1);
            }
            entries.add(e);
        }
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public void setEntries(List<Entry> entries) {
        this.entries = entries;
    }

    public int getData_rate() {
        return data_rate;
    }

    public void setData_rate(int data_rate) {
        this.data_rate = data_rate;
    }

    public int getNumIndSub() {
        return numIndSub;
    }

    public void setNumIndSub(int numIndSub) {
        this.numIndSub = numIndSub;
    }

    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public static class Entry {
        public int fscod;
        public int bsid;
        public int bsmod;
        public int acmod;
        public int lfeon;
        public int reserved;
        public int num_dep_sub;
        public int chan_loc;
        public int reserved2;


        @Override
        public String toString() {
            return "Entry{" +
                    "fscod=" + fscod +
                    ", bsid=" + bsid +
                    ", bsmod=" + bsmod +
                    ", acmod=" + acmod +
                    ", lfeon=" + lfeon +
                    ", reserved=" + reserved +
                    ", num_dep_sub=" + num_dep_sub +
                    ", chan_loc=" + chan_loc +
                    ", reserved2=" + reserved2 +
                    '}';
        }
    }
}
