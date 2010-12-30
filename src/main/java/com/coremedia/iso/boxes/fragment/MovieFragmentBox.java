/*
 * Copyright 2009 castLabs GmbH, Berlin
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

package com.coremedia.iso.boxes.fragment;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.AbstractContainerBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.MediaDataBox;
import com.coremedia.iso.boxes.TrackBoxContainer;
import com.coremedia.iso.boxes.TrackMetaData;
import com.coremedia.iso.mdta.Chunk;
import com.coremedia.iso.mdta.SampleImpl;
import com.coremedia.iso.mdta.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * aligned(8) class MovieFragmentBox extends Box(moof){
 * }
 */

public class MovieFragmentBox extends AbstractContainerBox implements TrackBoxContainer<TrackFragmentBox> {
    public static final String TYPE = "moof";
    private IsoBufferWrapper  isoBufferWrapper;

    public MovieFragmentBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    public String getDisplayName() {
        return "Movie Fragment Box";
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        // super does everything right but we need the IsoBufferWrapper for later
        this.isoBufferWrapper = in;
    }

    public List<TrackRunBox> getTrunsWithRealOffsets() {
        List<TrackRunBox> result = new ArrayList<TrackRunBox>();

        //assumption: a trun is semantically identically to a chunk
        //for each traf:
        // getBox(tfhd)#getBaseDataOffset +
        //   for each getBoxes(trun) -> trun#isDataOffsetPresent ? trun#getDataOffset : sum(size of all previous truns in traf)

        TrackFragmentBox[] trackFragmentBoxes = getBoxes(TrackFragmentBox.class);
        for (TrackFragmentBox trackFragmentBox : trackFragmentBoxes) {

            TrackFragmentHeaderBox trackFragmentHeaderBox = trackFragmentBox.getTrackFragmentHeaderBox();
            long baseDataOffset = trackFragmentHeaderBox.getBaseDataOffset();

            long cumulatedTrunBoxLength = 0;
            TrackRunBox[] trackRunBoxes = trackFragmentBox.getBoxes(TrackRunBox.class);
            for (TrackRunBox trackRunBox : trackRunBoxes) {
                if (trackRunBox.isDataOffsetPresent()) {
                    trackRunBox.setRealOffset(baseDataOffset + trackRunBox.getDataOffset());
                } else {
                    trackRunBox.setRealOffset(baseDataOffset + cumulatedTrunBoxLength);
                }

                result.add(trackRunBox);

                cumulatedTrunBoxLength += trackRunBox.getSize();
            }
        }

        return result;
    }

    public int getTrackCount() {
        return getBoxes(TrackFragmentBox.class).length;
    }

    /**
     * Returns the track numbers associated with this <code>MovieBox</code>.
     *
     * @return the tracknumbers (IDs) of the tracks in their order of appearance in the file
     */
    public long[] getTrackNumbers() {

        TrackFragmentBox[] trackBoxes = this.getBoxes(TrackFragmentBox.class);
        long[] trackNumbers = new long[trackBoxes.length];
        for (int trackCounter = 0; trackCounter < trackBoxes.length; trackCounter++) {
            TrackFragmentBox trackBoxe = trackBoxes[trackCounter];
            trackNumbers[trackCounter] = trackBoxe.getTrackFragmentHeaderBox().getTrackId();
        }
        return trackNumbers;
    }

    public void parseMdat(MediaDataBox<TrackFragmentBox> mdat) {
        mdat.getTrackMap().clear();

        List<TrackRunBox> truns = getTrunsWithRealOffsets();

        TreeMap<Long, Track<TrackFragmentBox>> trackIdsToTracksWithChunks = new TreeMap<Long, Track<TrackFragmentBox>>();

        long[] trackNumbers = getTrackNumbers();
        for (long trackNumber : trackNumbers) {
            TrackMetaData<TrackFragmentBox> trackMetaData = getTrackMetaData(trackNumber);
            trackIdsToTracksWithChunks.put(trackNumber, new Track<TrackFragmentBox>(trackNumber, trackMetaData, mdat));
        }


        for (TrackRunBox trackRunBox : truns) { //truns are comparable to Chunks with their offsets in non fragmented files

            TrackFragmentBox trackFragmentBox = (TrackFragmentBox) trackRunBox.getParent();
            long trackId = trackFragmentBox.getTrackFragmentHeaderBox().getTrackId();

            long trunOffset = trackRunBox.getRealOffset();
            //todo fix this. see TrackFragmentHeaderBox#getBaseDataOffset
            if (trunOffset == 0) {
                trunOffset = mdat.getStartOffset();
            }

            //chunk inside this mdat?
            if (mdat.getStartOffset() > trunOffset || trunOffset > mdat.getStartOffset() + mdat.getSizeIfNotParsed()) {
                System.out.println("Trun realOffset " + trunOffset + " not contained in " + this);
                continue;
            }

            long[] sampleOffsets = trackRunBox.getSampleOffsets();
            long[] sampleSizes = trackRunBox.getSampleSizes();

            for (int i = 1; i < sampleSizes.length; i++) {
                assert sampleOffsets[i] == sampleSizes[i - 1] + sampleOffsets[i - 1];
            }

            Track<TrackFragmentBox> parentTrack = trackIdsToTracksWithChunks.get(trackId);
            Chunk<TrackFragmentBox> chunk = new Chunk<TrackFragmentBox>(parentTrack, mdat, sampleSizes.length);
            parentTrack.addChunk(chunk);

            mdat.getTrackMap().put(parentTrack.getTrackId(), parentTrack);

            for (int i = 0; i < sampleSizes.length; i++) {
                MediaDataBox.SampleHolder<TrackFragmentBox> sh =
                        new MediaDataBox.SampleHolder<TrackFragmentBox>(new SampleImpl<TrackFragmentBox>(isoBufferWrapper, trunOffset + sampleOffsets[i], sampleSizes[i], chunk));
                mdat.getSampleList().add(sh);
                chunk.addSample(sh);
            }
        }

    }

    public TrackMetaData<TrackFragmentBox> getTrackMetaData(long trackId) {
        TrackFragmentBox[] trackBoxes = this.getBoxes(TrackFragmentBox.class);
        for (TrackFragmentBox trackFragmentBox : trackBoxes) {
            if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackId) {
                return new TrackMetaData<TrackFragmentBox>(trackId, trackFragmentBox);
            }
        }
        throw new RuntimeException("TrackId " + trackId + " not contained in " + this);
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("MovieFragmentBox[");
        Box[] boxes = getBoxes();
        for (int i = 0; i < boxes.length; i++) {
            if (i > 0) {
                builder.append(";");
            }
            builder.append(boxes[i].toString());
        }
        builder.append("]");
        return builder.toString();
    }

}
