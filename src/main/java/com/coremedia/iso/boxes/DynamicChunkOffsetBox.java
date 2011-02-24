package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.mdta.Chunk;
import com.coremedia.iso.mdta.Track;

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

  @Override
  public long getSize() {
    final long contentSize = getContentSize();
    return //box header
            4 + // size
            4 + // type
            (contentSize >= 4294967296L ? 8 : 0) +
           //full box header
            4 +
           //content
            contentSize +
           //rest
            getDeadBytes().length;
  }

  @Override
    protected long getContentSize() {
        long count = 0;
        IsoFile isoFile = this.getIsoFile();

        MediaDataBox<?>[] mdats = isoFile.getBoxes(MediaDataBox.class);
        for (MediaDataBox<?> mdat : mdats) {
          final Track<?> track = mdat.getTrack(trackId);
          if (track == null) {
            //System.out.println("mdat doesn't contain track with trackId " + trackId + " but " + mdat.getTracks());
            continue;
          }
          List<? extends Chunk<? extends TrackMetaDataContainer>> chunks = track.getChunks();
            count += chunks.size();
        }

        return count * 4 + 4;
    }

    @Override
    public long[] getChunkOffsets() {
      IsoFile isoFile = this.getIsoFile();

        MediaDataBox<?>[] mdats = isoFile.getBoxes(MediaDataBox.class);
        ArrayList<Long> chunkOffsets = new ArrayList<Long>();
        for (MediaDataBox<?> mdat : mdats) {
            long mdatStart = mdat.getOffset();
          final Track<?> track = mdat.getTrack(trackId);
          if (track == null) {
            //System.out.println("mdat doesn't contain track with trackId " + trackId + " but " + mdat.getTracks());
            continue;
          }
            List<? extends Chunk<? extends TrackMetaDataContainer>> chunks = track.getChunks();
            for (Chunk<?> chunk : chunks) {
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
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        throw new RuntimeException("A DynamicChunkOffsetBox cannot be filled with content by parsing. " +
                "It needs to be constructed from a StaticChunkOffsetBox");
    }

  public String toString() {
      return "DynamicChunkOffsetBox[entryCount=" + getChunkOffsets().length + "]";
  }


}
