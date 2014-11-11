package com.googlecode.mp4parser.authoring.samples;

import com.coremedia.iso.boxes.*;
import com.googlecode.mp4parser.authoring.Sample;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.AbstractList;
import java.util.List;
import java.util.UUID;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Created by sannies on 25.05.13.
 */
public class DefaultMp4SampleList extends AbstractList<Sample> {
    Container topLevel;
    TrackBox trackBox = null;
    ByteBuffer[] cache = null;
    int[] chunkNumsStartSampleNum;
    long[] chunkOffsets;
    int[] chunkSizes;
    int[][] chunkSampleSizeSums;
    SampleSizeBox ssb;
    int lastChunk = 0;


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
        chunkSizes = new int[chunkOffsets.length];

        cache = new ByteBuffer[chunkOffsets.length];
        chunkSampleSizeSums = new int[chunkOffsets.length][];
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
            chunkSampleSizeSums[currentChunkNo-1] = new int[currentSamplePerChunk];

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

        currentChunkNo=0;
        int sampleSum = 0;
        for (int i = 1; i<= ssb.getSampleCount(); i++)  {
            if (i == chunkNumsStartSampleNum[currentChunkNo]) {
                currentChunkNo++;
                sampleSum = 0;

            }
            chunkSizes[currentChunkNo-1] += ssb.getSampleSizeAtIndex(i-1);
            chunkSampleSizeSums[currentChunkNo-1][i - chunkNumsStartSampleNum[currentChunkNo-1]] = sampleSum;
            sampleSum +=ssb.getSampleSizeAtIndex(i-1);
        }

    }

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
        if (index >= ssb.getSampleCount()) {
            throw new IndexOutOfBoundsException();
        }

        int currentChunkNoZeroBased = getChunkForSample(index);

        int currentSampleNo = chunkNumsStartSampleNum[currentChunkNoZeroBased];

        long offset = chunkOffsets[l2i(currentChunkNoZeroBased)];
        ByteBuffer chunk = cache[l2i(currentChunkNoZeroBased)];
        if (chunk == null) {

            try {
                chunk = topLevel.getByteBuffer(offset, chunkSizes[l2i(currentChunkNoZeroBased)] );
                cache[l2i(currentChunkNoZeroBased)] = chunk;
            } catch (IOException e) {
                throw new IndexOutOfBoundsException(e.getMessage());
            }
        }


        int offsetWithinChunk = chunkSampleSizeSums[l2i(currentChunkNoZeroBased)][index - (currentSampleNo - 1)];
        final long sampleSize = ssb.getSampleSizeAtIndex(index);

        final ByteBuffer finalChunk = chunk;
        final int finalOffsetWithinChunk = offsetWithinChunk;
        return new Sample() {

            public void writeTo(WritableByteChannel channel) throws IOException {
                channel.write(asByteBuffer());
            }

            public long getSize() {
                return sampleSize;
            }

            public ByteBuffer asByteBuffer() {
                return (ByteBuffer) ((ByteBuffer) finalChunk.position(finalOffsetWithinChunk)).slice().limit(l2i(sampleSize));
            }

            @Override
            public String toString() {
                return "DefaultMp4Sample(size:" + sampleSize + ")";
            }
        };
    }

    @Override
    public int size() {
        return l2i(trackBox.getSampleTableBox().getSampleSizeBox().getSampleCount());
    }


    private class Calc {
        private int index;
        private int currentSampleNo;
        private int offsetWithinChunk;
        private long sampleSize;

        public Calc(int index, int currentSampleNo) {
            this.index = index;
            this.currentSampleNo = currentSampleNo;
        }

        public int getOffsetWithinChunk() {
            return offsetWithinChunk;
        }

        public long getSampleSize() {
            return sampleSize;
        }

        public Calc invoke() {
            offsetWithinChunk = 0;
            while (currentSampleNo < index + 1) {
                offsetWithinChunk  += ssb.getSampleSizeAtIndex((currentSampleNo++) - 1);
            }
            sampleSize = ssb.getSampleSizeAtIndex(currentSampleNo - 1);
            return this;
        }
    }
}
