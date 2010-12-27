package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.mdta.Chunk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DynamicChunkOffsetBox extends ChunkOffsetBox {
  long trackId;


  public DynamicChunkOffsetBox(ChunkOffsetBox chunkOffsetBox) {
    trackId = ((TrackBox) chunkOffsetBox.getParent().getParent().getParent().getParent()).getTrackHeaderBox().getTrackId();
    setParent(chunkOffsetBox.getParent());

  }

  protected long getContentSize() {
    long count = 0;
    MediaDataBox[] mdats = this.getIsoFile().getBoxes(MediaDataBox.class);
    for (MediaDataBox mdat : mdats) {
      List<Chunk> chunks = mdat.getTrack(trackId).getChunks();
      count += chunks.size();
    }

    return count * 4 + 4;
  }

  public long[] getChunkOffsets() {
    MediaDataBox[] mdats = this.getIsoFile().getBoxes(MediaDataBox.class);
    ArrayList<Long> chunkOffsets = new ArrayList<Long>();
    for (MediaDataBox mdat : mdats) {
      long mdatStart = mdat.calculateOffset();
      List<Chunk> chunks = mdat.getTrack(trackId).getChunks();
      for (Chunk chunk : chunks) {
        chunkOffsets.add(mdatStart + chunk.calculateOffset());
      }
    }
    long[] rc = new long[chunkOffsets.size()];
    for (int i = 0; i < rc.length; i++) {
      rc[i] = chunkOffsets.get(i);
    }
    return rc;
  }


  @Override
  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    throw new RuntimeException("A DynamicChunkOffsetBox cannot be filled with content by parsing. " +
            "It needs to be constructed from a StaticChunkOffsetBox");
  }

}
