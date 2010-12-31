/*  
 * Copyright 2008 CoreMedia AG, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an AS IS BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.coremedia.iso.mdta.Chunk;
import com.coremedia.iso.mdta.Sample;
import com.coremedia.iso.mdta.SampleImpl;
import com.coremedia.iso.mdta.Track;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This box contains the media data. In video tracks, this box would contain video frames. A presentation may
 * contain zero or more Media Data Boxes. The actual media data follows the type field; its structure is described
 * by the metadata (see {@link SampleTableBox}).<br>
 * In large presentations, it may be desirable to have more data in this box than a 32-bit size would permit. In this
 * case, the large variant of the size field is used.<br>
 * There may be any number of these boxes in the file (including zero, if all the media data is in other files). The
 * metadata refers to media data by its absolute offset within the file (see {@link StaticChunkOffsetBox});
 * so Media Data Box headers and free space may easily be skipped, and files without any box structure may
 * also be referenced and used.
 */
public final class MediaDataBox<T extends TrackMetaDataContainer> extends Box {
    public static final String TYPE = "mdat";

    private boolean contentsParsed = false;

    private byte[] deadBytesBefore = new byte[0];

    private long sizeIfNotParsed;

    private long startOffset;


    private Map<Long, Track<T>> tracks = new HashMap<Long, Track<T>>();

    private List<SampleHolder<T>> sampleList = new ArrayList<SampleHolder<T>>();
    private TrackBoxContainer<TrackFragmentBox> movieFragmentBoxBefore;
    private IsoBufferWrapper isoBufferWrapper;

    public MediaDataBox(MovieFragmentBox lastMovieFragmentBox) {
        super(IsoFile.fourCCtoBytes(TYPE));
        this.movieFragmentBoxBefore = lastMovieFragmentBox;
    }

    public boolean isContentsParsed() {
        return contentsParsed;
    }

    public byte[] getDeadBytesBefore() {
        return deadBytesBefore;
    }


    public void getBox(IsoOutputStream os) throws IOException {
        os.write(getHeader());
        os.write(getDeadBytesBefore());
        getContent(os);
        for (ByteBuffer buffer : deadBytes) {
            buffer.rewind();
            byte[] bufAsAr = new byte[buffer.limit()];
            buffer.get(bufAsAr);
            os.write(bufAsAr);
        }

    }

    public long getSize() {
        long contentSize = getContentSize();  // avoid calling getContentSize() twice
        long headerSize = 4 + // headerSize
                4 + // type
                (contentSize >= 4294967296L ? 8 : 0) +
                (Arrays.equals(getType(), IsoFile.fourCCtoBytes("uuid")) ? 16 : 0);
        return headerSize + contentSize + getDeadBytes().length + getDeadBytesBefore().length;
    }

    public long getSampleCount() {
        return sampleList.size();
    }

    public Sample<T> getSample(int index) {
        return sampleList.get(index).getSample();
    }

    public void replaceSample(int index, Sample s) {
        sampleList.get(index).setSample(s);
    }

    protected long getContentSize() {
        if (contentsParsed) {
            long size = 0;

            for (Track<T> track : tracks.values()) {
                size += track.getSize();
            }
            long size2 = 0;

            for (SampleHolder<T> sample : sampleList) {
                size2 += sample.getSample().getSize();
            }
            assert size == size2;
            return size;
        } else {
            return sizeIfNotParsed;
        }
    }

    public Track<T> getTrack(long trackId) {
        return tracks.get(trackId);
    }

    public Map<Long, Track<T>> getTrackMap() {
        return tracks;
    }

    public List<Track<T>> getTracks() {
        List<Track<T>> l = new LinkedList<Track<T>>(tracks.values());
        Collections.sort(l);
        return l;
    }


    public void parse(final IsoBufferWrapper in, long size, BoxParser boxParser, BoxInterface lastMovieFragmentBox) throws IOException {
        this.movieFragmentBoxBefore = (MovieFragmentBox) lastMovieFragmentBox;
        this.isoBufferWrapper = in;
        startOffset = in.position();
        sizeIfNotParsed = size;
        in.skip(size);
    }

    /**
     * Parses the mdat box's track, chunks and samples. Cannot be called before
     * the MovieBox has been parsed.
     *
     * @throws IOException in case of an error when reading the original iso file
     */
    public void parseTrackChunkSample() throws IOException {
        if (!contentsParsed) {


            ContainerBox bc = this.getParent();
            while (bc.getParent() != null) {
                bc = bc.getParent();
            }
            TrackBoxContainer<TrackBox>[] movieBoxes = bc.getBoxes(MovieBox.class);
            TrackBoxContainer<T> trackBoxContainer;
            if (movieFragmentBoxBefore == null) {
                trackBoxContainer = (TrackBoxContainer<T>) movieBoxes[0];
            } else {
                //fragmented
                trackBoxContainer = (TrackBoxContainer<T>) movieFragmentBoxBefore;
            }

            trackBoxContainer.parseMdat(this);

            //Collections.sort(sampleList);
            if (sampleList.size() > 0) {
                SampleImpl<T> firstSample = (SampleImpl<T>) sampleList.get(0).getSample();
                int bytesToFirstSample = (int) (firstSample.getOffset() - startOffset);
                if (bytesToFirstSample < 0) {
                    System.out.println("First sample offset smaller than startOffset of mdat. First sample: " + firstSample);
                    deadBytesBefore = new byte[0];
                } else {
                    deadBytesBefore = new byte[bytesToFirstSample];
                    isoBufferWrapper.position(offset + getHeaderSize());
                    isoBufferWrapper.read(deadBytesBefore);
                }
            } else {
                deadBytesBefore = new byte[0];
            }


            long endsAt = startOffset + deadBytesBefore.length;
            long lastOffset = 0;
            for (SampleHolder<T> sampleHolder : sampleList) {
                long newOffset = ((SampleImpl<T>) sampleHolder.getSample()).getOffset();
                assert newOffset > lastOffset :
                        "The samples are not in order. Their offsets are not strictly monotonic increasing.";
                assert endsAt == newOffset :
                        "There is a gap between two samples: endsAt=" + endsAt + " newOffset=" + newOffset +
                                " sampleIndex=" + sampleList.indexOf(sampleHolder);
                endsAt += sampleHolder.getSample().getSize();
            }

            contentsParsed = true;
        }
        // if already parsed: do nothing
    }

    public String getDisplayName() {
        return "Media Data Box";
    }


    protected void getContent(IsoOutputStream os) throws IOException {
        if (contentsParsed) {
            long sp = os.getStreamPosition();
            for (SampleHolder<T> sample : sampleList) {
                sample.getSample().getContent(os);
            }
            assert getContentSize() == os.getStreamPosition() - sp;
        } else {
            ByteBuffer[] segments = isoBufferWrapper.getSegment(startOffset, sizeIfNotParsed);
            for (ByteBuffer segment : segments) {
                while (segment.remaining() > 1024) {
                    byte[] buf = new byte[1024];
                    segment.get(buf);
                    os.write(buf);
                }
                while (segment.remaining() > 0) {
                    os.write(segment.get());
                }
            }
        }
    }

    public boolean isFragment() {
        return movieFragmentBoxBefore != null;
    }

    public String toString() {
        return "MediaDataBox[]";
    }

    public List<SampleHolder<T>> getSampleList() {
        return sampleList;
    }


    /**
     * This class acts as a kind of reference so that a sample may be replaced in a list of samples without
     * having to remove and add (Performance!!!).
     */
    public static class SampleHolder<T extends TrackMetaDataContainer> {
        public SampleHolder(Sample<T> sample) {
            this.sample = sample;
        }

        private Sample<T> sample;

        public Sample<T> getSample() {
            return sample;
        }

        public void setSample(Sample<T> sample) {
            this.sample = sample;
        }

        public String toString() {
            return "SampleHolder: " + sample.toString();
        }
    }

    public void removeTrack(Track<? extends TrackMetaDataContainer> track) {

        for (Chunk<? extends TrackMetaDataContainer> chunk : track.getChunks()) {
            for (Sample<? extends TrackMetaDataContainer> sample : chunk.getSamples()) {
                if (sample.getParent().getParentTrack().equals(track)) {
                    sampleList.remove(sample);
                }
            }
        }
    }

    public long getSizeIfNotParsed() {
        return sizeIfNotParsed;
    }

    public long getStartOffset() {
        return startOffset;
    }
}
