package com.mp4parser.boxes.iso14496.part12;

import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.IsoTypeWriter;

import java.nio.ByteBuffer;

import static com.mp4parser.tools.CastUtils.l2i;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
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
    public void setChunkOffsets(long[] chunkOffsets) {
        this.chunkOffsets = chunkOffsets;
    }

    @Override
    protected long getContentSize() {
        return 8 + 8 * chunkOffsets.length;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        int entryCount = l2i(IsoTypeReader.readUInt32(content));
        chunkOffsets = new long[entryCount];
        for (int i = 0; i < entryCount; i++) {
            chunkOffsets[i] = IsoTypeReader.readUInt64(content);
        }
    }

    @Override
    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, chunkOffsets.length);
        for (long chunkOffset : chunkOffsets) {
            IsoTypeWriter.writeUInt64(byteBuffer, chunkOffset);
        }
    }


}
