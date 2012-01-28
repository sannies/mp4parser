package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFileConvenienceHelper;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackRunBox;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 */
public class SampleList extends AbstractList<IsoBufferWrapper> {

    SortedMap<Long, Long> offsets2Sizes;
    List<Long> offsetKeys = null;

    IsoBufferWrapper isoBufferWrapper;

    private List<Long> getOffsetKeys() {
        if (offsetKeys == null) {
            List<Long> offsetKeys = new ArrayList<Long>(offsets2Sizes.size());
            for (Long aLong : offsets2Sizes.keySet()) {
                offsetKeys.add(aLong);
            }
            this.offsetKeys = offsetKeys;
        }
        return offsetKeys;
    }

    public SampleList(MovieFragmentBox moof) {
        assert 1 == moof.getTrackCount();
        offsets2Sizes = getOffsets(moof, moof.getTrackNumbers()[0]);
    }

    SortedMap<Long, Long> getOffsets(MovieFragmentBox moof, long trackId) {
        isoBufferWrapper = moof.getIsoFile().getOriginalIso();
        SortedMap<Long, Long> offsets2Sizes = new TreeMap<Long, Long>();
        List<TrackFragmentBox> traf = moof.getBoxes(TrackFragmentBox.class);
        for (TrackFragmentBox trackFragmentBox : traf) {
            if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackId) {
                long baseDataOffset;
                if (trackFragmentBox.getTrackFragmentHeaderBox().hasBaseDataOffset()) {
                    baseDataOffset = trackFragmentBox.getTrackFragmentHeaderBox().getBaseDataOffset();
                } else {
                    baseDataOffset = moof.getOffset();
                }

                for (TrackRunBox trun: trackFragmentBox.getBoxes(TrackRunBox.class)) {
                    long sampleBaseOffset = baseDataOffset + trun.getDataOffset();
                    long[] sampleOffsets = trun.getSampleOffsets();
                    long[] sampleSizes = trun.getSampleSizes();
                    for (int i = 0; i < sampleSizes.length; i++) {
                        offsets2Sizes.put(sampleOffsets[i] + sampleBaseOffset, sampleSizes[i]);
                    }
                }
            }
        }
        return offsets2Sizes;
    }

    public SampleList(TrackBox trackBox) {
        isoBufferWrapper = trackBox.getIsoFile().getOriginalIso();
        List<MovieExtendsBox> movieExtendsBoxes = trackBox.getParent().getBoxes(MovieExtendsBox.class);
        offsets2Sizes = new TreeMap<Long, Long>();
        if (movieExtendsBoxes.size() > 0) {
            List<TrackExtendsBox> trackExtendsBoxes = movieExtendsBoxes.get(0).getBoxes(TrackExtendsBox.class);
            for (TrackExtendsBox trackExtendsBox : trackExtendsBoxes) {
                if (trackExtendsBox.getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                    for (MovieFragmentBox movieFragmentBox : trackBox.getIsoFile().getBoxes(MovieFragmentBox.class)) {
                        offsets2Sizes.putAll(getOffsets(movieFragmentBox, trackBox.getTrackHeaderBox().getTrackId()));
                    }
                    return;
                }
            }
        }
        // No else since some tracks may be fragmented but not our track!

        SampleSizeBox sampleSizeBox = getSampleTableBox(trackBox).getSampleSizeBox();
        ChunkOffsetBox chunkOffsetBox = getSampleTableBox(trackBox).getChunkOffsetBox();
        SampleToChunkBox sampleToChunkBox = getSampleTableBox(trackBox).getSampleToChunkBox();
        long[] numberOfSamplesInChunk = sampleToChunkBox.blowup(chunkOffsetBox.getChunkOffsets().length);
        int sampleIndex = 0;
        if (sampleSizeBox.getSampleSize() > 0) {
            // Every sample has the same size!
            offsets2Sizes = new DummySortedMap<Long, Long>(sampleSizeBox.getSampleSize());
        }
        for (int i = 0; i < numberOfSamplesInChunk.length; i++) {
            long thisChunksNumberOfSamples = numberOfSamplesInChunk[i];
            long sampleOffset = chunkOffsetBox.getChunkOffsets()[i];
            for (int j = 0; j < thisChunksNumberOfSamples; j++) {
                long sampleSize = sampleSizeBox.getSampleSizeAtIndex(sampleIndex);
                offsets2Sizes.put(sampleOffset, sampleSize);
                sampleOffset += sampleSize;
                sampleIndex++;
            }
        }


    }

    @Override
    public IsoBufferWrapper get(int index) {
        // it is a two stage lookup: from index to offset to size
        Long offset = getOffsetKeys().get(index);
        try {
            return isoBufferWrapper.getSegment(offset, offsets2Sizes.get(offset));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public long getOffset(int index) {
        Iterator<Long> entries = offsets2Sizes.keySet().iterator();
        Long entry = entries.next();
        while (index > 0) {
            index--;
            entry = entries.next();
        }
        return offsets2Sizes.get(entry);
    }

    @Override
    public int size() {
        return offsets2Sizes.size();
    }

    private static SampleTableBox getSampleTableBox(TrackBox trackBox) {
        return (SampleTableBox) IsoFileConvenienceHelper.get(trackBox, "mdia/minf/stbl");
    }


}