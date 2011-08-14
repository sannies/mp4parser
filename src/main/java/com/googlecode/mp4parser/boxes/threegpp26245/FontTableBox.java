package com.googlecode.mp4parser.boxes.threegpp26245;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class FontTableBox extends AbstractBox {
    List<FontRecord> entries = new LinkedList<FontRecord>();

    public FontTableBox() {
        super(IsoFile.fourCCtoBytes("ftab"));
    }

    public void parse(IsoBufferWrapper in) {

    }

    public void getContent(IsoOutputStream isos) throws IOException {
        isos.writeUInt16(entries.size());
        for (FontRecord record : entries) {
            record.getContent(isos);
        }
    }

    @Override
    protected long getContentSize() {
        int size = 2;
        for (FontRecord fontRecord : entries) {
            size += fontRecord.getSize();
        }
        return size;
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        int numberOfRecords = in.readUInt16();
        for (int i = 0; i < numberOfRecords; i++) {
            FontRecord fr = new FontRecord();
            fr.parse(in);
            entries.add(fr);
        }
    }

    public List<FontRecord> getEntries() {
        return entries;
    }

    public void setEntries(List<FontRecord> entries) {
        this.entries = entries;
    }

    public static class FontRecord {
        int fontId;
        String fontname;

        public FontRecord() {
        }

        public FontRecord(int fontId, String fontname) {
            this.fontId = fontId;
            this.fontname = fontname;
        }

        public void parse(IsoBufferWrapper in) throws IOException {
            fontId = in.readUInt16();
            int length = in.readUInt8();
            fontname = in.readString(length);
        }

        public void getContent(IsoOutputStream isos) throws IOException {
            isos.writeUInt16(fontId);
            isos.writeUInt8(fontname.length());
            isos.writeStringNoTerm(fontname);
        }

        public int getSize() {
            return utf8StringLengthInBytes(fontname) + 3;
        }

        @Override
        public String toString() {
            return "FontRecord{" +
                    "fontId=" + fontId +
                    ", fontname='" + fontname + '\'' +
                    '}';
        }
    }
}
