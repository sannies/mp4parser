package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * Abstract Chunk Offset Box
 */
public class ChunkOffset64BitBox extends ChunkOffsetBox {
    public static final String TYPE = "co64";
    private long[] chunkOffsets;

    public ChunkOffset64BitBox() {
        super(TYPE);
    }

    @Override
    public long[] getChunkOffsets() {
        return chunkOffsets;
    }

    @Override
    protected long getContentSize() {
        return 4 + 8 * chunkOffsets.length;
    }


    protected void getContent(IsoOutputStream os) throws IOException {
        final long[] chunkOffsets = getChunkOffsets();
        os.writeUInt32(chunkOffsets.length);
        for (long chunkOffet : chunkOffsets) {
            os.writeUInt64(chunkOffet);
        }
    }

    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        long entryCount = in.readUInt32();
        if (entryCount > Integer.MAX_VALUE) {
            throw new IOException("The parser cannot deal with more than Integer.MAX_VALUE entries!");
        }
        chunkOffsets = new long[(int) entryCount];
        for (int i = 0; i < entryCount; i++) {
            chunkOffsets[i] = in.readUInt64();
        }
    }
}
