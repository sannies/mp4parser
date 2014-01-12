package com.googlecode.mp4parser.authoring.samples;

import com.coremedia.iso.boxes.*;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.SampleImpl;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.List;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Created by sannies on 25.05.13.
 */
public class DefaultMp4SampleList extends AbstractList<Sample> {
    Container topLevel;
    TrackBox trackBox = null;
    SoftReference<Sample>[] cache = null;
    int[] chunkNumsStartSampleNum;
    long[] chunkOffsets;
    SampleSizeBox ssb;

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
        chunkOffsets = trackBox.getSampleTableBox().getChunkOffsetBox().getChunkOffsets();
        cache = (SoftReference<Sample>[]) Array.newInstance(SoftReference.class, size());
        ssb = trackBox.getSampleTableBox().getSampleSizeBox();
        List<SampleToChunkBox.Entry> s2chunkEntries = trackBox.getSampleTableBox().getSampleToChunkBox().getEntries();
        SampleToChunkBox.Entry[] entries = s2chunkEntries.toArray(new SampleToChunkBox.Entry[s2chunkEntries.size()]);

        int s2cIndex = 0;
        SampleToChunkBox.Entry next = entries[s2cIndex++];
        int currentChunkNo = 0;
        int currentSamplePerChunk = 0;

        long nextFirstChunk = next.getFirstChunk();
        int nextSamplePerChunk = l2i(next.getSamplesPerChunk());

        int currentSampleNo = 1;
        int lastSampleNo = size();


        do {

            currentChunkNo++;
            if (currentChunkNo == nextFirstChunk) {
                currentSamplePerChunk = nextSamplePerChunk;
                if (entries.length > s2cIndex) {
                    next = entries[s2cIndex++];
                    nextSamplePerChunk = l2i(next.getSamplesPerChunk());
                    nextFirstChunk = next.getFirstChunk();
                } else {
                    nextSamplePerChunk = -1;
                    nextFirstChunk = Long.MAX_VALUE;
                }
            }

        } while ((currentSampleNo += currentSamplePerChunk) <= lastSampleNo);
        chunkNumsStartSampleNum = new int[currentChunkNo + 1];
        // reset of algorithm
        s2cIndex = 0;
        next = entries[s2cIndex++];
        currentChunkNo = 0;
        currentSamplePerChunk = 0;

        nextFirstChunk = next.getFirstChunk();
        nextSamplePerChunk = l2i(next.getSamplesPerChunk());

        currentSampleNo = 1;
        do {
            chunkNumsStartSampleNum[currentChunkNo++] = currentSampleNo;
            if (currentChunkNo == nextFirstChunk) {
                currentSamplePerChunk = nextSamplePerChunk;
                if (entries.length > s2cIndex) {
                    next = entries[s2cIndex++];
                    nextSamplePerChunk = l2i(next.getSamplesPerChunk());
                    nextFirstChunk = next.getFirstChunk();
                } else {
                    nextSamplePerChunk = -1;
                    nextFirstChunk = Long.MAX_VALUE;
                }
            }

        } while ((currentSampleNo += currentSamplePerChunk) <= lastSampleNo);
        chunkNumsStartSampleNum[currentChunkNo] = Integer.MAX_VALUE;

    }


    int lastChunk = 0;

    synchronized int getChunkForSample(int index) {
        int sampleNum = index + 1;
        // we always look for the next chunk in the last one to make linear access fast
        if (sampleNum >= chunkNumsStartSampleNum[lastChunk] && sampleNum < chunkNumsStartSampleNum[lastChunk + 1]) {
            return lastChunk;
        } else if (sampleNum < chunkNumsStartSampleNum[lastChunk]) {
            // we could search backwards but i don't believe there is much backward linear access
            // I'd then rather suspect a start from scratch
            lastChunk = 0;

            while (chunkNumsStartSampleNum[lastChunk + 1] <= sampleNum) {
                lastChunk++;
            }
            return lastChunk;

        } else {
            lastChunk += 1;

            while (chunkNumsStartSampleNum[lastChunk + 1] <= sampleNum) {
                lastChunk++;
            }
            return lastChunk;
        }

    }

    @Override
    public Sample get(int index) {
        if (index >= cache.length) {
            throw new IndexOutOfBoundsException();
        }
        if ((cache[index] != null) && (cache[index].get() != null)) {
            return cache[index].get();
        }

        int currentChunkNoZeroBased = getChunkForSample(index);

        int currentSampleNo = chunkNumsStartSampleNum[currentChunkNoZeroBased];

        long offset = chunkOffsets[l2i(currentChunkNoZeroBased)];

        while (currentSampleNo < index + 1) {
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
