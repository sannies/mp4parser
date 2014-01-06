package com.googlecode.mp4parser.authoring.samples;

import com.coremedia.iso.boxes.*;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.SampleImpl;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Iterator;
import java.util.List;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Created by sannies on 25.05.13.
 */
public class DefaultMp4SampleList extends AbstractList<Sample> {
    Container topLevel;
    TrackBox trackBox = null;
    SoftReference<Sample>[] cache = null;

    public DefaultMp4SampleList(long track, Container topLevel) {
        this.topLevel = topLevel;
        MovieBox movieBox = topLevel.getBoxes(MovieBox.class).get(0);
        List<TrackBox> trackBoxes = movieBox.getBoxes(TrackBox.class);

        for (TrackBox tb : trackBoxes) {
            if (tb.getTrackHeaderBox().getTrackId() == track) {
                trackBox = tb;
            }
        }
        if (trackBox == null) {
            throw new RuntimeException("This MP4 does not contain track " + track);
        }
        cache = (SoftReference<Sample>[]) Array.newInstance(SoftReference.class, size());
    }


    @Override
    public Sample get(int index) {
        if (index >= cache.length) {
            throw new IndexOutOfBoundsException();
        }
        if ((cache[index] != null) && (cache[index].get() != null)) {
            return cache[index].get();
        }

        List<SampleToChunkBox.Entry> entries = trackBox.getSampleTableBox().getSampleToChunkBox().getEntries();
        Iterator<SampleToChunkBox.Entry> iterator = entries.iterator();
        SampleToChunkBox.Entry next = iterator.next();
        long currentChunkNo = 0;
        long currentSamplePerChunk = 0;

        long nextFirstChunk = next.getFirstChunk();
        long nextSamplePerChunk = next.getSamplesPerChunk();

        int currentSampleNo = 1;
        int targetSampleNo = index + 1;


        do {

            currentChunkNo++;
            if (currentChunkNo == nextFirstChunk) {
                currentSamplePerChunk = nextSamplePerChunk;
                if (iterator.hasNext()) {
                    next = iterator.next();
                    nextSamplePerChunk = next.getSamplesPerChunk();
                    nextFirstChunk = next.getFirstChunk();
                } else {
                    nextSamplePerChunk = -1;
                    nextFirstChunk = Long.MAX_VALUE;
                }
            }

        } while ((currentSampleNo += currentSamplePerChunk) <= targetSampleNo);
        currentSampleNo -= currentSamplePerChunk;

        long offset = trackBox.getSampleTableBox().getChunkOffsetBox().getChunkOffsets()[l2i(currentChunkNo - 1)];
        SampleSizeBox ssb = trackBox.getSampleTableBox().getSampleSizeBox();
        while (currentSampleNo < targetSampleNo) {
            offset += ssb.getSampleSizeAtIndex((currentSampleNo++) - 1);
        }
        final long sampleSize = ssb.getSampleSizeAtIndex(currentSampleNo - 1);
        SampleImpl sampleImpl = new SampleImpl(offset, sampleSize, topLevel);
        cache[index] = new SoftReference<Sample>(sampleImpl);
        return sampleImpl;
    }

    @Override
    public int size() {
        return l2i(trackBox.getSampleTableBox().getSampleSizeBox().getSampleCount());
    }


}
