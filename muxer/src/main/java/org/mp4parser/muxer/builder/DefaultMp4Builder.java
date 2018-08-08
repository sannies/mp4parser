/*
 * Copyright 2012 Sebastian Annies, Hamburg
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
package org.mp4parser.muxer.builder;

import org.mp4parser.*;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.boxes.iso23001.part7.SampleEncryptionBox;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.samplegrouping.GroupEntry;
import org.mp4parser.boxes.samplegrouping.SampleGroupDescriptionBox;
import org.mp4parser.boxes.samplegrouping.SampleToGroupBox;
import org.mp4parser.muxer.Edit;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.tracks.encryption.CencEncryptedTrack;
import org.mp4parser.tools.IsoTypeWriter;
import org.mp4parser.tools.Mp4Arrays;
import org.mp4parser.tools.Offsets;
import org.mp4parser.tools.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static org.mp4parser.tools.CastUtils.l2i;
import static org.mp4parser.tools.Mp4Math.lcm;

/**
 * Creates a plain MP4 file from a video. Plain as plain can be.
 */
public class DefaultMp4Builder implements Mp4Builder {

    private static Logger LOG = LoggerFactory.getLogger(DefaultMp4Builder.class);
    Map<Track, StaticChunkOffsetBox> chunkOffsetBoxes = new HashMap<>();
    Set<SampleAuxiliaryInformationOffsetsBox> sampleAuxiliaryInformationOffsetsBoxes = new HashSet<>();
    HashMap<Track, List<Sample>> track2Sample = new HashMap<>();
    HashMap<Track, long[]> track2SampleSizes = new HashMap<>();
    private Fragmenter fragmenter;   
    private boolean useLongestTrackTimeScale = false;
    private boolean explicitElstSegmentDuration = true;
    
    
    private static long sum(int[] ls) {
        long rc = 0;
        for (long l : ls) {
            rc += l;
        }
        return rc;
    }

    private static long sum(long[] ls) {
        long rc = 0;
        for (long l : ls) {
            rc += l;
        }
        return rc;
    }


    public void setFragmenter(Fragmenter fragmenter) {
        this.fragmenter = fragmenter;
    }
    
    
    /** 
     * Indicates if the longest track time scale is used in the <code>Movie</code>
     * @return 
     */
    public boolean useLongestTrackTimeScale() {
        return useLongestTrackTimeScale;
    }

    /**
     * Indicates if the longest track time scale should be used in the <code>Movie</code>
     * @param use <code>true</code> for longest track time scale, otherwise, a resolution of 1000 milliseconds will be used.
     */
    public void setLongestTrackTimeScale(boolean use) {
        useLongestTrackTimeScale = use;
    }

    /**
     * Indicates if the track duration should be used in the segment duration of <i>edit list</i>.
     * This applies if there is only one entry in the <i>edit list</i> and the<i>segment duration</i> field is zero
     * @return 
     */
    public boolean isExplicitElstSegmentDuration() {
        return explicitElstSegmentDuration;
    }

    /**
     * Indicates if the track duration should be used in the segment duration of <i>edit list</i>.
     * This applies if there is only one entry in the <i>edit list</i> and the <i>segment duration</i> field is zero
     * @param explicit <code>false</code> the original entry will be used
     */
    public void setExplicitElstSegmentDuration(boolean explicit) {
        this.explicitElstSegmentDuration = explicit;
    }


    @Override
    public Container build(Movie movie) {
        if (fragmenter == null) {
            fragmenter = new DefaultFragmenterImpl(2);
        }
        LOG.debug("Creating movie {}", movie);
        for (Track track : movie.getTracks()) {
            // getting the samples may be a time consuming activity
            List<Sample> samples = track.getSamples();
            putSamples(track, samples);
            long[] sizes = new long[samples.size()];
            for (int i = 0; i < sizes.length; i++) {
                Sample b = samples.get(i);
                sizes[i] = b.getSize();
            }
            track2SampleSizes.put(track, sizes);

        }

        BasicContainer isoFile = new BasicContainer();

        isoFile.addBox(createFileTypeBox(movie));

        Map<Track, int[]> chunks = new HashMap<Track, int[]>();
        for (Track track : movie.getTracks()) {
            chunks.put(track, getChunkSizes(track));
        }
        ParsableBox moov = createMovieBox(movie, chunks);
        isoFile.addBox(moov);
        List<SampleSizeBox> stszs = Path.getPaths(moov, "trak/mdia/minf/stbl/stsz");

        long contentSize = 0;
        for (SampleSizeBox stsz : stszs) {
            contentSize += sum(stsz.getSampleSizes());

        }
        LOG.debug("About to create mdat");
        InterleaveChunkMdat mdat = new InterleaveChunkMdat(movie, chunks, contentSize);

        long dataOffset = 16;
        for (Box lightBox : isoFile.getBoxes()) {
            dataOffset += lightBox.getSize();
        }
        isoFile.addBox(mdat);
        LOG.debug("mdat crated");

        /*
        dataOffset is where the first sample starts. In this special mdat the samples always start
        at offset 16 so that we can use the same offset for large boxes and small boxes
         */

        for (StaticChunkOffsetBox chunkOffsetBox : chunkOffsetBoxes.values()) {
            long[] offsets = chunkOffsetBox.getChunkOffsets();
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] += dataOffset;
            }
        }
        for (SampleAuxiliaryInformationOffsetsBox saio : sampleAuxiliaryInformationOffsetsBoxes) {
            long offset = saio.getSize(); // the calculation is systematically wrong by 4, I don't want to debug why. Just a quick correction --san 14.May.13
            offset += 4 + 4 + 4 + 4 + 4 + 24;
            // size of all header we were missing otherwise (moov, trak, mdia, minf, stbl)
            offset = Offsets.find(isoFile, saio, offset);

            long[] saioOffsets = saio.getOffsets();
            for (int i = 0; i < saioOffsets.length; i++) {
                saioOffsets[i] = saioOffsets[i] + offset;

            }
            saio.setOffsets(saioOffsets);
        }


        return isoFile;
    }

    protected List<Sample> putSamples(Track track, List<Sample> samples) {
        return track2Sample.put(track, samples);
    }

    protected FileTypeBox createFileTypeBox(Movie movie) {
        List<String> minorBrands = new LinkedList<String>();

        minorBrands.add("mp42");
        minorBrands.add("iso6");
        minorBrands.add("avc1");
        minorBrands.add("isom");
        return new FileTypeBox("iso6", 1, minorBrands);
    }

    protected MovieBox createMovieBox(Movie movie, Map<Track, int[]> chunks) {
        MovieBox movieBox = new MovieBox();
        MovieHeaderBox mvhd = new MovieHeaderBox();

        mvhd.setCreationTime(new Date());
        mvhd.setModificationTime(new Date());
        mvhd.setMatrix(movie.getMatrix());
        long timeScale = useLongestTrackTimeScale ? getTimescale(movie) : 1000;
        long[] trackDurations = new long[chunks.size()];
        int i = 0;
        
        long longestTrack = 0;
        long nextTrackId = 0;

        for ( ; i< trackDurations.length ; i++) {
            Track track = movie.getTracks().get(i);
            trackDurations[i] = (long)Math.ceil(calculateTrackDuration(track) * timeScale);
            
            if (trackDurations[i] > longestTrack) {
                longestTrack = trackDurations[i];
            }
            
            // find the next available trackId
            nextTrackId = nextTrackId < track.getTrackMetaData().getTrackId() ? track.getTrackMetaData().getTrackId() : nextTrackId;
        }

        mvhd.setDuration(longestTrack);
        mvhd.setTimescale(timeScale);

        mvhd.setNextTrackId(++nextTrackId);

        movieBox.addBox(mvhd);
        for (i=0 ; i<trackDurations.length; i++) {
            movieBox.addBox(createTrackBox(movie.getTracks().get(i), movie, chunks, trackDurations[i]));
        }
        // metadata here
        ParsableBox udta = createUdta(movie);
        if (udta != null) {
            movieBox.addBox(udta);
        }
        return movieBox;

    }

    /***
     * Calculte the duration of the given track in seconds
     * @param track the desired track
     * @return the amount of seconds (aprox.)
     */
    protected double calculateTrackDuration(Track track) {
        List<Edit> edits = track.getEdits();
        if (edits == null || edits.isEmpty() || edits.size() == 1 && edits.get(0).getSegmentDuration() < 1) {
            return track.getDuration() / (double)track.getTrackMetaData().getTimescale();
        }
        
        double d = 0;
        for (Edit edit : edits) {
            d += edit.getSegmentDuration();
        }
        
        return d * track.getTrackMetaData().getTimescale();
    }
    
    /**
     * Override to create a user data box that may contain metadata.
     *
     * @param movie source movie
     * @return a 'udta' box or <code>null</code> if none provided
     */
    protected ParsableBox createUdta(Movie movie) {
        return null;
    }

    protected TrackBox createTrackBox(Track track, Movie movie, Map<Track, int[]> chunks, long duration) {

        TrackBox trackBox = new TrackBox();
        TrackHeaderBox tkhd = new TrackHeaderBox();

        tkhd.setEnabled(true);
        tkhd.setInMovie(true);
//        tkhd.setInPreview(true);
//        tkhd.setInPoster(true);
        tkhd.setMatrix(track.getTrackMetaData().getMatrix());

        tkhd.setAlternateGroup(track.getTrackMetaData().getGroup());
        tkhd.setCreationTime(track.getTrackMetaData().getCreationTime());

        tkhd.setDuration(duration);

        tkhd.setHeight(track.getTrackMetaData().getHeight());
        tkhd.setWidth(track.getTrackMetaData().getWidth());
        tkhd.setLayer(track.getTrackMetaData().getLayer());
        tkhd.setModificationTime(new Date());
        tkhd.setTrackId(track.getTrackMetaData().getTrackId());
        tkhd.setVolume(track.getTrackMetaData().getVolume());

        trackBox.addBox(tkhd);

        trackBox.addBox(createEdts(track, movie, duration));

        MediaBox mdia = new MediaBox();
        trackBox.addBox(mdia);
        MediaHeaderBox mdhd = new MediaHeaderBox();
        mdhd.setCreationTime(track.getTrackMetaData().getCreationTime());
        mdhd.setDuration(track.getDuration());
        mdhd.setTimescale(track.getTrackMetaData().getTimescale());
        mdhd.setLanguage(track.getTrackMetaData().getLanguage());
        mdia.addBox(mdhd);
        HandlerBox hdlr = new HandlerBox();
        mdia.addBox(hdlr);

        hdlr.setHandlerType(track.getHandler());

        MediaInformationBox minf = new MediaInformationBox();
        if (track.getHandler().equals("vide")) {
            minf.addBox(new VideoMediaHeaderBox());
            hdlr.setName("VideoHandler");
        } else if (track.getHandler().equals("soun")) {
            minf.addBox(new SoundMediaHeaderBox());
            hdlr.setName("AudioHandler");
        } else if (track.getHandler().equals("text")) {
            minf.addBox(new NullMediaHeaderBox());
        } else if (track.getHandler().equals("subt")) {
            minf.addBox(new SubtitleMediaHeaderBox());
        } else if (track.getHandler().equals("hint")) {
            minf.addBox(new HintMediaHeaderBox());
        } else if (track.getHandler().equals("sbtl")) {
            minf.addBox(new NullMediaHeaderBox());
        }

        // dinf: all these three boxes tell us is that the actual
        // data is in the current file and not somewhere external
        DataInformationBox dinf = new DataInformationBox();
        DataReferenceBox dref = new DataReferenceBox();
        dinf.addBox(dref);
        DataEntryUrlBox url = new DataEntryUrlBox();
        url.setFlags(1);
        dref.addBox(url);
        minf.addBox(dinf);
        //

        ParsableBox stbl = createStbl(track, movie, chunks);
        minf.addBox(stbl);
        mdia.addBox(minf);
        LOG.debug("done with trak for track_{}", track.getTrackMetaData().getTrackId());
        return trackBox;
    }

    protected ParsableBox createEdts(Track track, Movie movie, long trackDuration) {
        if (track.getEdits() != null && track.getEdits().size() > 0) {
            EditListBox elst = new EditListBox();
            elst.setVersion(0); // quicktime won't play file when version = 1
            List<EditListBox.Entry> entries = new ArrayList<>();

            for (Edit edit : track.getEdits()) {
                entries.add(new EditListBox.Entry(elst,
                        Math.round(edit.getSegmentDuration() * movie.getTimescale()),
                        edit.getMediaTime() * track.getTrackMetaData().getTimescale() / edit.getTimeScale(),
                        edit.getMediaRate()));
            }
            
            if (explicitElstSegmentDuration && entries.size() == 1 && entries.get(0).getSegmentDuration() == 0) {
                entries.get(0).setSegmentDuration(trackDuration); // required for thumbnail providers
            }

            elst.setEntries(entries);
            EditBox edts = new EditBox();
            edts.addBox(elst);
            return edts;
        } else {
            return null;
        }
    }

    protected ParsableBox createStbl(Track track, Movie movie, Map<Track, int[]> chunks) {
        SampleTableBox stbl = new SampleTableBox();

        createStsd(track, stbl);
        createStts(track, stbl);
        createCtts(track, stbl);
        createStss(track, stbl);
        createSdtp(track, stbl);
        createStsc(track, chunks, stbl);
        createStsz(track, stbl);
        createStco(track, movie, chunks, stbl);


        Map<String, List<GroupEntry>> groupEntryFamilies = new HashMap<String, List<GroupEntry>>();
        for (Map.Entry<GroupEntry, long[]> sg : track.getSampleGroups().entrySet()) {
            String type = sg.getKey().getType();
            List<GroupEntry> groupEntries = groupEntryFamilies.get(type);
            if (groupEntries == null) {
                groupEntries = new ArrayList<>();
                groupEntryFamilies.put(type, groupEntries);
            }
            groupEntries.add(sg.getKey());
        }
        for (Map.Entry<String, List<GroupEntry>> sg : groupEntryFamilies.entrySet()) {
            SampleGroupDescriptionBox sgdb = new SampleGroupDescriptionBox();
            String type = sg.getKey();
            sgdb.setGroupingType(type);
            sgdb.setGroupEntries(sg.getValue());
            SampleToGroupBox sbgp = new SampleToGroupBox();
            sbgp.setGroupingType(type);
            SampleToGroupBox.Entry last = null;
            for (int i = 0; i < track.getSamples().size(); i++) {
                int index = 0;
                for (int j = 0; j < sg.getValue().size(); j++) {
                    GroupEntry groupEntry = sg.getValue().get(j);
                    long[] sampleNums = track.getSampleGroups().get(groupEntry);
                    if (Arrays.binarySearch(sampleNums, i) >= 0) {
                        index = j + 1;
                    }
                }
                if (last == null || last.getGroupDescriptionIndex() != index) {
                    last = new SampleToGroupBox.Entry(1, index);
                    sbgp.getEntries().add(last);
                } else {
                    last.setSampleCount(last.getSampleCount() + 1);
                }
            }
            stbl.addBox(sgdb);
            stbl.addBox(sbgp);
        }

        if (track instanceof CencEncryptedTrack) {
            createCencBoxes((CencEncryptedTrack) track, stbl, chunks.get(track));
        }
        createSubs(track, stbl);
        LOG.debug("done with stbl for track_{}", track.getTrackMetaData().getTrackId());
        return stbl;
    }

    protected void createSubs(Track track, SampleTableBox stbl) {
        if (track.getSubsampleInformationBox() != null) {
            stbl.addBox(track.getSubsampleInformationBox());
        }
    }

    protected void createCencBoxes(CencEncryptedTrack track, SampleTableBox stbl, int[] chunkSizes) {

        SampleAuxiliaryInformationSizesBox saiz = new SampleAuxiliaryInformationSizesBox();

        saiz.setAuxInfoType("cenc");
        saiz.setFlags(1);
        List<CencSampleAuxiliaryDataFormat> sampleEncryptionEntries = track.getSampleEncryptionEntries();
        if (track.hasSubSampleEncryption()) {
            short[] sizes = new short[sampleEncryptionEntries.size()];
            for (int i = 0; i < sizes.length; i++) {
                sizes[i] = (short) sampleEncryptionEntries.get(i).getSize();
            }
            saiz.setSampleInfoSizes(sizes);
        } else {
            saiz.setDefaultSampleInfoSize(8); // 8 bytes iv
            saiz.setSampleCount(track.getSamples().size());
        }

        SampleAuxiliaryInformationOffsetsBox saio = new SampleAuxiliaryInformationOffsetsBox();
        SampleEncryptionBox senc = new SampleEncryptionBox();
        senc.setSubSampleEncryption(track.hasSubSampleEncryption());
        senc.setEntries(sampleEncryptionEntries);

        long offset = senc.getOffsetToFirstIV();
        int index = 0;
        long[] offsets = new long[chunkSizes.length];


        for (int i = 0; i < chunkSizes.length; i++) {
            offsets[i] = offset;
            for (int j = 0; j < chunkSizes[i]; j++) {
                offset += sampleEncryptionEntries.get(index++).getSize();
            }
        }
        saio.setOffsets(offsets);

        stbl.addBox(saiz);
        stbl.addBox(saio);
        stbl.addBox(senc);
        sampleAuxiliaryInformationOffsetsBoxes.add(saio);


    }

    protected void createStsd(Track track, SampleTableBox stbl) {
        SampleDescriptionBox stsd = new SampleDescriptionBox();
        stsd.setBoxes(track.getSampleEntries());
        stbl.addBox(stsd);
    }

    protected void createStco(Track targetTrack, Movie movie, Map<Track, int[]> chunks, SampleTableBox stbl) {
        if (chunkOffsetBoxes.get(targetTrack) == null) {
            // The ChunkOffsetBox we create here is just a stub
            // since we haven't created the whole structure we can't tell where the
            // first chunk starts (mdat box). So I just let the chunk offset
            // start at zero and I will add the mdat offset later.

            long offset = 0;
            // all tracks have the same number of chunks
            LOG.debug("Calculating chunk offsets for track_{}", targetTrack.getTrackMetaData().getTrackId());

            List<Track> tracks = new ArrayList<Track>(chunks.keySet());
            Collections.sort(tracks, new Comparator<Track>() {
                public int compare(Track o1, Track o2) {
                    return l2i(o1.getTrackMetaData().getTrackId() - o2.getTrackMetaData().getTrackId());
                }
            });
            Map<Track, Integer> trackToChunk = new HashMap<Track, Integer>();
            Map<Track, Integer> trackToSample = new HashMap<Track, Integer>();
            Map<Track, Double> trackToTime = new HashMap<Track, Double>();
            for (Track track : tracks) {
                trackToChunk.put(track, 0);
                trackToSample.put(track, 0);
                trackToTime.put(track, 0.0);
                chunkOffsetBoxes.put(track, new StaticChunkOffsetBox());
            }

            while (true) {
                Track nextChunksTrack = null;
                for (Track track : tracks) {
                    // This always chooses the least progressed track
                    if ((nextChunksTrack == null || trackToTime.get(track) < trackToTime.get(nextChunksTrack)) &&
                            // either first OR track's next chunk's starttime is smaller than nextTrack's next chunks starttime
                            // AND their need to be chunks left!
                            (trackToChunk.get(track) < chunks.get(track).length)) {
                        nextChunksTrack = track;
                    }
                }
                if (nextChunksTrack == null) {
                    break; // no next
                }
                // found the next one
                ChunkOffsetBox chunkOffsetBox = chunkOffsetBoxes.get(nextChunksTrack);
                chunkOffsetBox.setChunkOffsets(Mp4Arrays.copyOfAndAppend(chunkOffsetBox.getChunkOffsets(), offset));

                int nextChunksIndex = trackToChunk.get(nextChunksTrack);

                int numberOfSampleInNextChunk = chunks.get(nextChunksTrack)[nextChunksIndex];
                int startSample = trackToSample.get(nextChunksTrack);
                double time = trackToTime.get(nextChunksTrack);

                long[] durs = nextChunksTrack.getSampleDurations();
                for (int j = startSample; j < startSample + numberOfSampleInNextChunk; j++) {
                    offset += track2SampleSizes.get(nextChunksTrack)[j];
                    time += (double) durs[j] / nextChunksTrack.getTrackMetaData().getTimescale();
                }
                trackToChunk.put(nextChunksTrack, nextChunksIndex + 1);
                trackToSample.put(nextChunksTrack, startSample + numberOfSampleInNextChunk);
                trackToTime.put(nextChunksTrack, time);
            }

        }

        stbl.addBox(chunkOffsetBoxes.get(targetTrack));
    }

    protected void createStsz(Track track, SampleTableBox stbl) {
        SampleSizeBox stsz = new SampleSizeBox();
        stsz.setSampleSizes(track2SampleSizes.get(track));

        stbl.addBox(stsz);
    }

    protected void createStsc(Track track, Map<Track, int[]> chunks, SampleTableBox stbl) {
        int[] tracksChunkSizes = chunks.get(track);

        SampleToChunkBox stsc = new SampleToChunkBox();
        stsc.setEntries(new LinkedList<SampleToChunkBox.Entry>());
        long lastChunkSize = Integer.MIN_VALUE; // to be sure the first chunks hasn't got the same size
        long lastSampleDescriptionIndex = Integer.MIN_VALUE;
        List<Sample> samples = track.getSamples();

        int currentSampleIndex = 0;
        List<SampleEntry> sampleEntries = track.getSampleEntries();

        for (int i = 0; i < tracksChunkSizes.length; i++) {
            Sample sample = samples.get(currentSampleIndex);
            int currentSampleDescriptionIndex = sampleEntries.indexOf(sample.getSampleEntry()) + 1; // one base

            if (lastChunkSize != tracksChunkSizes[i] || lastSampleDescriptionIndex != currentSampleDescriptionIndex) {
                stsc.getEntries().add(new SampleToChunkBox.Entry(i + 1, tracksChunkSizes[i], currentSampleDescriptionIndex));
                lastChunkSize = tracksChunkSizes[i];
                lastSampleDescriptionIndex = currentSampleDescriptionIndex;
            }
            currentSampleIndex += tracksChunkSizes[i];
        }
        stbl.addBox(stsc);
    }

    protected void createSdtp(Track track, SampleTableBox stbl) {
        if (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty()) {
            SampleDependencyTypeBox sdtp = new SampleDependencyTypeBox();
            sdtp.setEntries(track.getSampleDependencies());
            stbl.addBox(sdtp);
        }
    }

    protected void createStss(Track track, SampleTableBox stbl) {
        long[] syncSamples = track.getSyncSamples();
        if (syncSamples != null && syncSamples.length > 0) {
            SyncSampleBox stss = new SyncSampleBox();
            stss.setSampleNumber(syncSamples);
            stbl.addBox(stss);
        }
    }

    protected void createCtts(Track track, SampleTableBox stbl) {
        List<CompositionTimeToSample.Entry> compositionTimeToSampleEntries = track.getCompositionTimeEntries();
        if (compositionTimeToSampleEntries != null && !compositionTimeToSampleEntries.isEmpty()) {
            CompositionTimeToSample ctts = new CompositionTimeToSample();
            ctts.setEntries(compositionTimeToSampleEntries);
            stbl.addBox(ctts);
        }
    }

    protected void createStts(Track track, SampleTableBox stbl) {
        TimeToSampleBox.Entry lastEntry = null;
        List<TimeToSampleBox.Entry> entries = new ArrayList<TimeToSampleBox.Entry>();

        for (long delta : track.getSampleDurations()) {
            if (lastEntry != null && lastEntry.getDelta() == delta) {
                lastEntry.setCount(lastEntry.getCount() + 1);
            } else {
                lastEntry = new TimeToSampleBox.Entry(1, delta);
                entries.add(lastEntry);
            }

        }
        TimeToSampleBox stts = new TimeToSampleBox();
        stts.setEntries(entries);
        stbl.addBox(stts);
    }

    /**
     * Gets the chunk sizes for the given track.
     *
     * @param track the track we are talking about
     * @return the size of each chunk in number of samples
     */
    int[] getChunkSizes(Track track) {

        long[] referenceChunkStarts = fragmenter.sampleNumbers(track);
        int[] chunkSizes = new int[referenceChunkStarts.length];


        for (int i = 0; i < referenceChunkStarts.length; i++) {
            long start = referenceChunkStarts[i] - 1;
            long end;
            if (referenceChunkStarts.length == i + 1) {
                end = track.getSamples().size();
            } else {
                end = referenceChunkStarts[i + 1] - 1;
            }

            chunkSizes[i] = l2i(end - start);
        }
        assert DefaultMp4Builder.this.track2Sample.get(track).size() == sum(chunkSizes) : "The number of samples and the sum of all chunk lengths must be equal";
        return chunkSizes;


    }

    public long getTimescale(Movie movie) {

        long timescale = movie.getTracks().iterator().next().getTrackMetaData().getTimescale();
        for (Track track : movie.getTracks()) {
            timescale = lcm(timescale, track.getTrackMetaData().getTimescale());
        }
        return timescale;
    }

    private class InterleaveChunkMdat implements Box {
        List<Track> tracks;
        List<List<Sample>> chunkList = new ArrayList<List<Sample>>();


        long contentSize;

        private InterleaveChunkMdat(Movie movie, Map<Track, int[]> chunks, long contentSize) {
            this.contentSize = contentSize;
            this.tracks = movie.getTracks();
            List<Track> tracks = new ArrayList<Track>(chunks.keySet());
            Collections.sort(tracks, new Comparator<Track>() {
                public int compare(Track o1, Track o2) {
                    return l2i(o1.getTrackMetaData().getTrackId() - o2.getTrackMetaData().getTrackId());
                }
            });
            Map<Track, Integer> trackToChunk = new HashMap<Track, Integer>();
            Map<Track, Integer> trackToSample = new HashMap<Track, Integer>();
            Map<Track, Double> trackToTime = new HashMap<Track, Double>();
            for (Track track : tracks) {
                trackToChunk.put(track, 0);
                trackToSample.put(track, 0);
                trackToTime.put(track, 0.0);
            }

            while (true) {
                Track nextChunksTrack = null;
                for (Track track : tracks) {
                    if ((nextChunksTrack == null || trackToTime.get(track) < trackToTime.get(nextChunksTrack)) &&
                            // either first OR track's next chunk's starttime is smaller than nextTrack's next chunks starttime
                            // AND their need to be chunks left!
                            (trackToChunk.get(track) < chunks.get(track).length)) {
                        nextChunksTrack = track;
                    }
                }
                if (nextChunksTrack == null) {
                    break;
                }
                // found the next one

                int nextChunksIndex = trackToChunk.get(nextChunksTrack);
                int numberOfSampleInNextChunk = chunks.get(nextChunksTrack)[nextChunksIndex];
                int startSample = trackToSample.get(nextChunksTrack);
                double time = trackToTime.get(nextChunksTrack);
                for (int j = startSample; j < startSample + numberOfSampleInNextChunk; j++) {
                    time += (double) nextChunksTrack.getSampleDurations()[j] / nextChunksTrack.getTrackMetaData().getTimescale();
                }
                chunkList.add(nextChunksTrack.getSamples().subList(startSample, startSample + numberOfSampleInNextChunk));

                trackToChunk.put(nextChunksTrack, nextChunksIndex + 1);
                trackToSample.put(nextChunksTrack, startSample + numberOfSampleInNextChunk);
                trackToTime.put(nextChunksTrack, time);
            }


        }

        public String getType() {
            return "mdat";
        }

        public long getSize() {
            return 16 + contentSize;
        }

        private boolean isSmallBox(long contentSize) {
            return (contentSize + 8) < 4294967296L;
        }


        public void getBox(WritableByteChannel writableByteChannel) throws IOException {
            ByteBuffer bb = ByteBuffer.allocate(16);
            long size = getSize();
            if (isSmallBox(size)) {
                IsoTypeWriter.writeUInt32(bb, size);
            } else {
                IsoTypeWriter.writeUInt32(bb, 1);
            }
            bb.put(IsoFile.fourCCtoBytes("mdat"));
            if (isSmallBox(size)) {
                bb.put(new byte[8]);
            } else {
                IsoTypeWriter.writeUInt64(bb, size);
            }
            bb.rewind();
            writableByteChannel.write(bb);
            long writtenBytes = 0;
            long writtenMegaBytes = 0;

            LOG.debug("About to write {}", contentSize);
            for (List<Sample> samples : chunkList) {
                for (Sample sample : samples) {
                    sample.writeTo(writableByteChannel);
                    writtenBytes += sample.getSize();
                    if (writtenBytes > 1024 * 1024) {
                        writtenBytes -= 1024 * 1024;
                        writtenMegaBytes++;
                        LOG.debug("Written {} MB", writtenMegaBytes);
                    }
                }
            }

        }

    }
}
