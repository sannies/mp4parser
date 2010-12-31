package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

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

    long[] sampleCount = new long[]{};
    long[] sampleOffset = new long[]{};


    public CompositionTimeToSample() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public long[] getSampleCount() {
        return sampleCount;
    }

    public long[] getSampleOffset() {
        return sampleOffset;
    }

    public void setSampleCountAndOffset(long[] sampleCount, long[] sampleOffset) {
        assert sampleCount.length == sampleOffset.length;
        this.sampleCount = sampleCount;
        this.sampleOffset = sampleOffset;
    }


    protected long getContentSize() {
        return 4 + 8 * sampleCount.length;
    }

    public String getDisplayName() {
        return "Composition Time to Sample Box";
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        long numberOfEntries = in.readUInt32();
        assert numberOfEntries <= Integer.MAX_VALUE : "Too many entries";
        sampleCount = new long[(int) numberOfEntries];
        sampleOffset = new long[(int) numberOfEntries];

        for (int i = 0; i < numberOfEntries; i++) {
            sampleCount[i] = in.readUInt32();
            sampleOffset[i] = in.readUInt32();
        }
    }

    protected void getContent(IsoOutputStream os) throws IOException {
        os.writeUInt32(sampleCount.length);
        for (int i = 0; i < sampleCount.length; i++) {
            os.writeUInt32(sampleCount[i]);
            os.writeUInt32(sampleOffset[i]);
        }
    }
}
