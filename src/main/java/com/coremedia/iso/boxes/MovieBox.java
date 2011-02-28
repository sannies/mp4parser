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
import com.coremedia.iso.mdta.Chunk;
import com.coremedia.iso.mdta.SampleImpl;
import com.coremedia.iso.mdta.Track;

import java.io.IOException;
import java.util.*;

/**
 * The metadata for a presentation is stored in the single Movie Box which occurs at the top-level of a file.
 * Normally this box is close to the beginning or end of the file, though this is not required.
 */
public class MovieBox extends AbstractContainerBox implements TrackBoxContainer<TrackBox> {
    public static final String TYPE = "moov";
    private IsoBufferWrapper isoBufferWrapper;

    public MovieBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

    @Override
    public String getDisplayName() {
        return "Movie Box";
    }

    @Override
    public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
        super.parse(in, size, boxParser, lastMovieFragmentBox);
        // super does everything fine but we need the IsoBufferWrapper for later
        this.isoBufferWrapper = in;
    }

    public long[] getChunkOffsets() {
        List<Long> offsets = new ArrayList<Long>();
        for (Box trackBoxe : this.getBoxes()) {
            if (trackBoxe instanceof TrackBox) {
                TrackBox trackBox = (TrackBox) trackBoxe;
                SampleTableBox sampleTableBox = null;
                // Do not find the way to the sampleTableBox by many getBoxes(Class) calls since they need to much
                // object instantiation. Going this way here speeds up the process.
                for (Box mediaBoxe : trackBox.getBoxes()) {
                    if (mediaBoxe instanceof MediaBox) {
                        for (Box mediaInformationBoxe : ((MediaBox) mediaBoxe).getBoxes()) {
                            if (mediaInformationBoxe instanceof MediaInformationBox) {
                                for (Box sampleTableBoxe : ((MediaInformationBox) mediaInformationBoxe).getBoxes()) {
                                    if (sampleTableBoxe instanceof SampleTableBox) {
                                        sampleTableBox = (SampleTableBox) sampleTableBoxe;
                                    }
                                }
                            }
                        }
                    }
                }
                if (sampleTableBox != null) {
                    ChunkOffsetBox chunkOffsetBox = sampleTableBox.getChunkOffsetBox();
                    if (chunkOffsetBox == null) {
                        System.out.println("SampleTableBox " + sampleTableBox + " of TrackBox " + trackBox + " doesn't contain a ChunkOffsetBox!");
                    } else {
                        long[] chunkOffsets = chunkOffsetBox.getChunkOffsets();
                        for (long chunkOffset : chunkOffsets) {
                            offsets.add(chunkOffset);
                        }
                    }
                }
            }
        }
        Collections.sort(offsets);
        long[] returnArray = new long[offsets.size()];
        for (int i = 0; i < returnArray.length; i++) {
            returnArray[i] = offsets.get(i);
        }
        return returnArray;
    }

    public long getTrackIdForChunk(long chunkOffset) {
        for (Box trackBoxe : this.getBoxes()) {
            if (trackBoxe instanceof TrackBox) {
                TrackBox trackBox = (TrackBox) trackBoxe;
                SampleTableBox sampleTableBox = null;
                // Do not find the way to the sampleTableBox by many getBoxes(Class) calls since they need to much
                // object instantiation. Going this way here speeds up the process.
                for (Box mediaBoxe : trackBox.getBoxes()) {
                    if (mediaBoxe instanceof MediaBox) {
                        for (Box mediaInformationBoxe : ((MediaBox) mediaBoxe).getBoxes()) {
                            if (mediaInformationBoxe instanceof MediaInformationBox) {
                                for (Box sampleTableBoxe : ((MediaInformationBox) mediaInformationBoxe).getBoxes()) {
                                    if (sampleTableBoxe instanceof SampleTableBox) {
                                        sampleTableBox = (SampleTableBox) sampleTableBoxe;
                                    }
                                }
                            }
                        }
                    }
                }
                if ((Arrays.binarySearch(sampleTableBox.getChunkOffsetBox().getChunkOffsets(), chunkOffset)) >= 0) {
                    return trackBox.getTrackHeaderBox().getTrackId();
                }
            }
        }
        throw new RuntimeException("wrong chunkOffset, the chunk's offset is in no chunk offset box of no track box");
    }

    public void parseMdat(MediaDataBox<TrackBox> mdat) {
        mdat.getTrackMap().clear();

        TreeMap<Long, Track<TrackBox>> trackIdsToTracksWithChunks = new TreeMap<Long, Track<TrackBox>>();

        long[] trackNumbers = getTrackNumbers();
        Map<Long, Long> trackToSampleCount = new HashMap<Long, Long>(trackNumbers.length);
        Map<Long, List<Long>> trackToSyncSamples = new HashMap<Long, List<Long>>();

        for (long trackNumber : trackNumbers) {
            TrackMetaData<TrackBox> trackMetaData = getTrackMetaData(trackNumber);
            trackIdsToTracksWithChunks.put(trackNumber, new Track<TrackBox>(trackNumber, trackMetaData, mdat));
            SyncSampleBox syncSampleBox = trackMetaData.getSyncSampleBox();
            if (syncSampleBox != null) {
                long[] sampleNumbers = syncSampleBox.getSampleNumber();
                ArrayList<Long> sampleNumberList = new ArrayList<Long>(sampleNumbers.length);
                for (long sampleNumber : sampleNumbers) {
                    sampleNumberList.add(sampleNumber);
                }
                trackToSyncSamples.put(trackNumber, sampleNumberList);
            }
            trackToSampleCount.put(trackNumber, 1L);
        }


        long[] chunkOffsets = getChunkOffsets();

        for (long chunkOffset : chunkOffsets) {
            //chunk inside this mdat?
            if (mdat.getStartOffset() > chunkOffset || chunkOffset > mdat.getStartOffset() + mdat.getSizeIfNotParsed()) {
                System.out.println("Chunk offset " + chunkOffset + " not contained in " + this);
                continue;
            }

            long track = getTrackIdForChunk(chunkOffset);

            long[] sampleOffsets = getSampleOffsetsForChunk(chunkOffset);
            long[] sampleSizes = getSampleSizesForChunk(chunkOffset);

            for (int i = 1; i < sampleSizes.length; i++) {
                assert sampleOffsets[i] == sampleSizes[i - 1] + sampleOffsets[i - 1];
            }

            Track<TrackBox> parentTrack = trackIdsToTracksWithChunks.get(track);
            Chunk<TrackBox> chunk = new Chunk<TrackBox>(parentTrack, mdat, sampleSizes.length);
            parentTrack.addChunk(chunk);

            long trackId = parentTrack.getTrackId();
            mdat.getTrackMap().put(trackId, parentTrack);

            for (int i = 0; i < sampleOffsets.length; i++) {
                Long currentSample = trackToSampleCount.get(trackId);
                List<Long> syncSamples = trackToSyncSamples.get(trackId);
                boolean syncSample = syncSamples != null && syncSamples.contains(currentSample);

                MediaDataBox.SampleHolder<TrackBox> sh =
                        new MediaDataBox.SampleHolder<TrackBox>(
                                new SampleImpl<TrackBox>(isoBufferWrapper, chunkOffset + sampleOffsets[i], sampleSizes[i], chunk, syncSample));
                mdat.getSampleList().add(sh);
                chunk.addSample(sh);
                trackToSampleCount.put(trackId, currentSample + 1);
            }
        }
    }

    public long[] getSampleSizesForChunk(long chunkOffset) {

        for (Box trackBoxe : this.getBoxes()) {
            if (trackBoxe instanceof TrackBox) {
                TrackBox trackBox = (TrackBox) trackBoxe;
                SampleTableBox sampleTableBox = null;
                // Do not find the way to the sampleTableBox by many getBoxes(Class) calls since they need to much
                // object instantiation. Going this way here speeds up the process.
                for (Box mediaBoxe : trackBox.getBoxes()) {
                    if (mediaBoxe instanceof MediaBox) {
                        for (Box mediaInformationBoxe : ((MediaBox) mediaBoxe).getBoxes()) {
                            if (mediaInformationBoxe instanceof MediaInformationBox) {
                                for (Box sampleTableBoxe : ((MediaInformationBox) mediaInformationBoxe).getBoxes()) {
                                    if (sampleTableBoxe instanceof SampleTableBox) {
                                        sampleTableBox = (SampleTableBox) sampleTableBoxe;
                                    }
                                }
                            }
                        }
                    }
                }
                int chunkNumber;
                final ChunkOffsetBox chunkOffsetBox = sampleTableBox.getChunkOffsetBox();
                long[] chunkOffsets = chunkOffsetBox.getChunkOffsets();
                if ((chunkNumber = Arrays.binarySearch(chunkOffsets, chunkOffset)) >= 0) {
                    long[] samplesPerChunk = new long[chunkOffsets.length];
                    SampleSizeBox sampleSizeBox = sampleTableBox.getSampleSizeBox();
                    SampleToChunkBox sampleToChunkBox = sampleTableBox.getSampleToChunkBox();

                    List<SampleToChunkBox.Entry> sampleToChunkEntries = new LinkedList<SampleToChunkBox.Entry>(sampleToChunkBox.getEntries());
                    Collections.reverse(sampleToChunkEntries);
                    Iterator<SampleToChunkBox.Entry> iterator = sampleToChunkEntries.iterator();
                    SampleToChunkBox.Entry currentEntry = iterator.next();

                    for (int i = samplesPerChunk.length; i > 1; i--) {
                        samplesPerChunk[i - 1] = currentEntry.getSamplesPerChunk();
                        if (i == currentEntry.getFirstChunk()) {
                            currentEntry = iterator.next();
                        }
                    }
                    samplesPerChunk[0] = currentEntry.getSamplesPerChunk();


                    int sampleNumberOffset = 0;
                    for (int i = 0; i < chunkNumber; i++) {
                        sampleNumberOffset += samplesPerChunk[i];
                    }
                    long noOfSamplesInThisChunk = samplesPerChunk[chunkNumber];
                    assert noOfSamplesInThisChunk <= Integer.MAX_VALUE : "The parser cannot deal with more than Integer.MAX_VALUE samples in a one chunk";
                    long[] returnValue = new long[(int) noOfSamplesInThisChunk];
                    for (int i = 0; i < samplesPerChunk[chunkNumber]; i++) {
                        returnValue[i] = sampleSizeBox.getSampleSizeAtIndex(i + sampleNumberOffset);
                    }
                    return returnValue;
                }
            }
        }
        throw new RuntimeException("wrong chunkOffset");
    }

    public long[] getSampleOffsetsForChunk(long chunkOffset) {
        long[] sizes = getSampleSizesForChunk(chunkOffset);
        long[] offsets = new long[sizes.length];
        for (int i = 1; i < sizes.length; i++) {
            offsets[i] = offsets[i - 1] + sizes[i - 1];
        }
        return offsets;
    }

    public int getTrackCount() {
        return getBoxes(TrackBox.class).length;
    }


    /**
     * Returns the track numbers associated with this <code>MovieBox</code>.
     *
     * @return the tracknumbers (IDs) of the tracks in their order of appearance in the file
     */
    public long[] getTrackNumbers() {

        AbstractBox[] trackBoxes = this.getBoxes(TrackBox.class);
        long[] trackNumbers = new long[trackBoxes.length];
        for (int trackCounter = 0; trackCounter < trackBoxes.length; trackCounter++) {
            AbstractBox trackBoxe = trackBoxes[trackCounter];
            TrackBox trackBox = (TrackBox) trackBoxe;
            trackNumbers[trackCounter] = trackBox.getTrackHeaderBox().getTrackId();
        }
        return trackNumbers;
    }

    public TrackMetaData<TrackBox> getTrackMetaData(long trackId) {
        TrackBox[] trackBoxes = this.getBoxes(TrackBox.class);
        for (TrackBox trackBox : trackBoxes) {
            if (trackBox.getTrackHeaderBox().getTrackId() == trackId) {
                return new TrackMetaData<TrackBox>(trackId, trackBox);
            }
        }
        throw new RuntimeException("TrackId " + trackId + " not contained in " + this);
    }

    public String toString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append("MovieBox[");
        Box[] boxes = getBoxes();
        for (int i = 0; i < boxes.length; i++) {
            if (i > 0) {
                buffer.append(";");
            }
            buffer.append(boxes[i].toString());
        }
        buffer.append("]");
        return buffer.toString();
    }

    public MovieHeaderBox getMovieHeaderBox() {
        for (Box box : boxes) {
            if (box instanceof MovieHeaderBox) {
                return (MovieHeaderBox) box;
            }
        }
        return null;
    }
}
