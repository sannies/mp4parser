package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * Abstract Chunk Offset Box
 */
public abstract class ChunkOffsetBox extends AbstractFullBox {
    public static final String TYPE = "stco";

    public ChunkOffsetBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public abstract long[] getChunkOffsets();

    public String getDisplayName() {
        return "Chunk Offset Box";
    }

    protected abstract long getContentSize();

    protected void getContent(IsoOutputStream os) throws IOException {
      final long[] chunkOffsets = getChunkOffsets();
      os.writeUInt32(chunkOffsets.length);
        for (long chunkOffet : chunkOffsets) {
            os.writeUInt32(chunkOffet);
        }
    }

    public String toString() {
        return "StaticChunkOffsetBox[entryCount=" + getChunkOffsets().length + "]";
    }

}
