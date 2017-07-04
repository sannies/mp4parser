package org.mp4parser.muxer.container.mp4;

import org.mp4parser.Container;
import org.mp4parser.boxes.iso14496.part12.MovieBox;
import org.mp4parser.boxes.iso14496.part12.SampleSizeBox;
import org.mp4parser.boxes.iso14496.part12.SampleToChunkBox;
import org.mp4parser.boxes.iso14496.part12.TrackBox;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.muxer.RandomAccessSource;
import org.mp4parser.muxer.Sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mp4parser.tools.CastUtils.l2i;


public class DefaultMp4SampleList extends AbstractList<Sample> {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultMp4SampleList.class);

    private TrackBox trackBox = null;
    private SoftReference<ByteBuffer>[] cache = null;
    private int[] chunkNumsStartSampleNum;
    private int[] chunkNumsToSampleDescriptionIndex;
    private long[] chunkOffsets;
    private long[][] sampleOffsetsWithinChunks;
    private SampleSizeBox ssb;
    private int lastChunk = 0;
    private RandomAccessSource randomAccess;
    private ArrayList<SampleEntry> sampleEntries;


    public DefaultMp4SampleList(long track, Container topLevel, RandomAccessSource randomAccessFile) {
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
        sampleEntries = new ArrayList<>(trackBox.getSampleTableBox().getSampleDescriptionBox().getBoxes(SampleEntry.class));

        if (sampleEntries.size() != trackBox.getSampleTableBox().getSampleDescriptionBox().getBoxes().size())
            throw new AssertionError("stsd contains not only sample entries. Something's wrong here! Bailing out");

        chunkOffsets = trackBox.getSampleTableBox().getChunkOffsetBox().getChunkOffsets();
        long[] chunkSizes = new long[chunkOffsets.length];

        cache = new SoftReference[chunkOffsets.length];
        Arrays.fill(cache, new SoftReference<ByteBuffer>(null));

        sampleOffsetsWithinChunks = new long[chunkOffsets.length][];
        chunkNumsToSampleDescriptionIndex = new int[chunkOffsets.length];
        ssb = trackBox.getSampleTableBox().getSampleSizeBox();
        List<SampleToChunkBox.Entry> s2chunkEntries = trackBox.getSampleTableBox().getSampleToChunkBox().getEntries();
        SampleToChunkBox.Entry[] entries = s2chunkEntries.toArray(new SampleToChunkBox.Entry[s2chunkEntries.size()]);


        int s2cIndex = 0;
        SampleToChunkBox.Entry next = entries[s2cIndex++];
        int currentChunkNo = 0;
        int currentSamplePerChunk = 0;
        int currentSampleDescriptionIndex = 0;


        long nextFirstChunk = next.getFirstChunk();
        int nextSamplePerChunk = l2i(next.getSamplesPerChunk());
        int nextSampleDescriptionIndex = l2i(next.getSampleDescriptionIndex());

        int currentSampleNo = 1;
        int lastSampleNo = size();


        do {

            currentChunkNo++;
            if (currentChunkNo == nextFirstChunk) {
                currentSamplePerChunk = nextSamplePerChunk;
                currentSampleDescriptionIndex = nextSampleDescriptionIndex;
                if (entries.length > s2cIndex) {
                    next = entries[s2cIndex++];
                    nextSamplePerChunk = l2i(next.getSamplesPerChunk());
                    nextSampleDescriptionIndex = l2i(next.getSampleDescriptionIndex());
                    nextFirstChunk = next.getFirstChunk();
                } else {
                    nextSamplePerChunk = -1;
                    nextSampleDescriptionIndex = -1;
                    nextFirstChunk = Long.MAX_VALUE;
                }
            }
            sampleOffsetsWithinChunks[currentChunkNo - 1] = new long[currentSamplePerChunk];
            chunkNumsToSampleDescriptionIndex[currentChunkNo -1] = currentSampleDescriptionIndex;

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

    private synchronized int getChunkForSample(int index) {
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
    public Sample get(final int index) {
        if (index >= ssb.getSampleCount()) {
            throw new IndexOutOfBoundsException();
        }
        return new SampleImpl(index);
    }

    @Override
    public int size() {
        return l2i(trackBox.getSampleTableBox().getSampleSizeBox().getSampleCount());
    }

    class SampleImpl implements Sample {

        private int index;

        public SampleImpl(int index) {
            this.index = index;
        }

        public void writeTo(WritableByteChannel channel) throws IOException {
            channel.write(asByteBuffer());
        }

        public long getSize() {
            return ssb.getSampleSizeAtIndex(index);
        }

        public synchronized ByteBuffer asByteBuffer() {
            ByteBuffer b;

            final int chunkNumber = getChunkForSample(index);
            SoftReference<ByteBuffer> chunkBufferSr = cache[chunkNumber];

            final int chunkStartSample = chunkNumsStartSampleNum[chunkNumber] - 1;

            int sampleInChunk = index - chunkStartSample;
            long[] sampleOffsetsWithinChunk = sampleOffsetsWithinChunks[l2i(chunkNumber)];
            long offsetWithInChunk = sampleOffsetsWithinChunk[sampleInChunk];

            ByteBuffer chunkBuffer;
            if (chunkBufferSr == null || (chunkBuffer = chunkBufferSr.get()) == null) {
                try {

                    chunkBuffer = randomAccess.get(
                            chunkOffsets[l2i(chunkNumber)],
                            sampleOffsetsWithinChunk[sampleOffsetsWithinChunk.length - 1] + ssb.getSampleSizeAtIndex(chunkStartSample + sampleOffsetsWithinChunk.length - 1));
                    cache[chunkNumber] = new SoftReference<ByteBuffer>(chunkBuffer);
                } catch (IOException e) {
                    LOG.error("", e);
                    throw new IndexOutOfBoundsException(e.getMessage());
                }
            }
            b = (ByteBuffer) ((ByteBuffer) chunkBuffer.duplicate().position(l2i(offsetWithInChunk))).slice().limit(l2i(ssb.getSampleSizeAtIndex(index)));
            return b;
        }

        @Override
        public String toString() {
            return "Sample(index: " + index + " size: " + ssb.getSampleSizeAtIndex(index) + ")";
        }

        @Override
        public SampleEntry getSampleEntry() {
            return sampleEntries.get(chunkNumsToSampleDescriptionIndex[getChunkForSample(this.index)]-1);
        }
    }

    ;

}
