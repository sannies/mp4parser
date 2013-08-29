package com.googlecode.mp4parser.authoring.samples;

import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleToChunkBox;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.SampleImpl;

import java.io.IOException;
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
    }


    @Override
    public Sample get(int index) {
        if (index >= trackBox.getSampleTableBox().getSampleSizeBox().getSampleCount()) {
            throw new IndexOutOfBoundsException();
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

        long chunkStart = trackBox.getSampleTableBox().getChunkOffsetBox().getChunkOffsets()[l2i(currentChunkNo - 1)];
        long offset = chunkStart;
        SampleSizeBox ssb = trackBox.getSampleTableBox().getSampleSizeBox();
        while (currentSampleNo < targetSampleNo) {
            offset += ssb.getSampleSizeAtIndex((currentSampleNo++) - 1);
        }
        try {
            return new SampleImpl(this.topLevel.getByteBuffer(offset, ssb.getSampleSizeAtIndex(currentSampleNo - 1)));
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public int size() {
        return l2i(trackBox.getSampleTableBox().getSampleSizeBox().getSampleCount());
    }


}
