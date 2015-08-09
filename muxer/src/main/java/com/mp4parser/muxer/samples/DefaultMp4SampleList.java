package com.mp4parser.muxer.samples;

import com.mp4parser.Container;
import com.mp4parser.muxer.RandomAccessSource;
import com.mp4parser.muxer.Sample;
import com.mp4parser.boxes.iso14496.part12.MovieBox;
import com.mp4parser.boxes.iso14496.part12.SampleSizeBox;
import com.mp4parser.boxes.iso14496.part12.SampleToChunkBox;
import com.mp4parser.boxes.iso14496.part12.TrackBox;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;

import static com.mp4parser.tools.CastUtils.l2i;


public class DefaultMp4SampleList extends AbstractList<Sample> {
    //private static final long MAX_MAP_SIZE = 4096 * 1024;
    private static final long MAX_MAP_SIZE = 1024 * 1024 * 256; // Limit maximum mem map to 512MB

    Container topLevel;
    TrackBox trackBox = null;
    SoftReference<ByteBuffer[]>[] cache = null;
    int[] chunkNumsStartSampleNum;
    long[] chunkOffsets;
    long[] chunkSizes;
    long[][] sampleOffsetsWithinChunks;
    SampleSizeBox ssb;
    int lastChunk = 0;
    private RandomAccessSource randomAccess;


    public DefaultMp4SampleList(long track, Container topLevel, RandomAccessSource randomAccessFile) {
        this.topLevel = topLevel;
        this.randomAccess = randomAccessFile;
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
        chunkSizes = new long[chunkOffsets.length];

        cache = (SoftReference<ByteBuffer[]>[]) Array.newInstance(SoftReference.class, chunkOffsets.length);
        sampleOffsetsWithinChunks = new long[chunkOffsets.length][];
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
            sampleOffsetsWithinChunks[currentChunkNo - 1] = new long[currentSamplePerChunk];

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

        currentChunkNo = 0;
        long sampleSum = 0;
        for (int i = 1; i <= ssb.getSampleCount(); i++) {
            while (i == chunkNumsStartSampleNum[currentChunkNo]) {
                // you might think that an if statement is enough but unfortunately you might as well declare chunks without any samples!
                currentChunkNo++;
                sampleSum = 0;
            }
            chunkSizes[currentChunkNo - 1] += ssb.getSampleSizeAtIndex(i - 1);
            long[] sampleOffsetsWithinChunkscurrentChunkNo = sampleOffsetsWithinChunks[currentChunkNo - 1];
            int chunkNumsStartSampleNumcurrentChunkNo = chunkNumsStartSampleNum[currentChunkNo - 1];
            sampleOffsetsWithinChunkscurrentChunkNo[i - chunkNumsStartSampleNumcurrentChunkNo] = sampleSum;
            sampleSum += ssb.getSampleSizeAtIndex(i - 1);
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

        int chunkNumber = getChunkForSample(index);
        int chunkStartSample = chunkNumsStartSampleNum[chunkNumber] - 1;
        long chunkOffset = chunkOffsets[l2i(chunkNumber)];
        int sampleInChunk = index - chunkStartSample;
        long[] sampleOffsetsWithinChunk = sampleOffsetsWithinChunks[l2i(chunkNumber)];
        long offsetWithInChunk = sampleOffsetsWithinChunk[sampleInChunk];


        SoftReference<ByteBuffer[]> chunkBuffersSr = cache[l2i(chunkNumber)];
        ByteBuffer[] chunkBuffers = chunkBuffersSr != null ? chunkBuffersSr.get() : null;
        if (chunkBuffers == null) {
            List<ByteBuffer> _chunkBuffers = new ArrayList<ByteBuffer>();
            long currentStart = 0;
            try {
                for (int i = 0; i < sampleOffsetsWithinChunk.length; i++) {
                    if (sampleOffsetsWithinChunk[i] + ssb.getSampleSizeAtIndex(i + chunkStartSample) - currentStart > MAX_MAP_SIZE) {
                        _chunkBuffers.add(randomAccess.get(
                                chunkOffset + currentStart,
                                (sampleOffsetsWithinChunk[i] - currentStart)));
                        currentStart = sampleOffsetsWithinChunk[i];
                    }
                }
                _chunkBuffers.add(randomAccess.get(chunkOffset + currentStart,
                        (-currentStart + sampleOffsetsWithinChunk[sampleOffsetsWithinChunk.length - 1] + ssb.getSampleSizeAtIndex(chunkStartSample + sampleOffsetsWithinChunk.length - 1))));
                chunkBuffers = _chunkBuffers.toArray(new ByteBuffer[_chunkBuffers.size()]);
                cache[l2i(chunkNumber)] = new SoftReference<ByteBuffer[]>(chunkBuffers);
            } catch (IOException e) {
                throw new IndexOutOfBoundsException(e.getMessage());
            }
        }

        ByteBuffer correctPartOfChunk = null;

        for (ByteBuffer chunkBuffer : chunkBuffers) {
            if (offsetWithInChunk < chunkBuffer.limit()) {
                correctPartOfChunk = chunkBuffer;
                break;
            }
            offsetWithInChunk -= chunkBuffer.limit();
        }


        final long sampleSize = ssb.getSampleSizeAtIndex(index);
        final ByteBuffer finalCorrectPartOfChunk = correctPartOfChunk;
        final long finalOffsetWithInChunk = offsetWithInChunk;
        return new Sample() {

            public void writeTo(WritableByteChannel channel) throws IOException {
                channel.write(asByteBuffer());
            }

            public long getSize() {
                return sampleSize;
            }

            public ByteBuffer asByteBuffer() {
                return (ByteBuffer) ((ByteBuffer) finalCorrectPartOfChunk.position(l2i(finalOffsetWithInChunk))).slice().limit(l2i(sampleSize));
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

}
