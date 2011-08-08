package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.AbstractBox;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.DataEntryUrlBox;
import com.coremedia.iso.boxes.DataInformationBox;
import com.coremedia.iso.boxes.DataReferenceBox;
import com.coremedia.iso.boxes.FileTypeBox;
import com.coremedia.iso.boxes.HandlerBox;
import com.coremedia.iso.boxes.HintMediaHeaderBox;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.MediaInformationBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.coremedia.iso.boxes.NullMediaHeaderBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.SampleToChunkBox;
import com.coremedia.iso.boxes.SoundMediaHeaderBox;
import com.coremedia.iso.boxes.StaticChunkOffsetBox;
import com.coremedia.iso.boxes.SyncSampleBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.coremedia.iso.boxes.VideoMediaHeaderBox;
import com.coremedia.iso.boxes.fragment.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import sun.net.ftp.FtpClient;
import sun.plugin.javascript.navig4.Link;

import java.io.IOException;
import java.security.acl.LastOwnerException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Creates a plain MP4 file from a video. Plain as plain can be.
 */
public class DefaultMp4IsoBuilder implements IsoBuilder {
    Set<StaticChunkOffsetBox> chunkOffsetBoxes = new HashSet<StaticChunkOffsetBox>();
    private static Logger LOG = Logger.getLogger(DefaultMp4IsoBuilder.class.getName());

    public IsoFile build(Movie movie) throws IOException {
        LOG.info("Creating movie " + movie);
        IsoFile isoFile = new IsoFile(new IsoBufferWrapperImpl(new byte[]{}));
        isoFile.parse();
        // ouch that is ugly but I don't know how to do it else
        Set<String> minorBrands = new HashSet<String>();
        minorBrands.add("isom");
        minorBrands.add("iso2");
        minorBrands.add("avc1");

        isoFile.addBox(new FileTypeBox("isom", 0, minorBrands));
        isoFile.addBox(createMovieBox(movie));
        Box mdat = new InterleaveChunkMdat(movie);
        isoFile.addBox(mdat);
        /*
        dataOffset is where the first sample starts. Since we created the chunk offset boxes
        without knowing this offset and temporarely
         */
        long dataOffset = mdat.calculateOffset() + 8;
        for (StaticChunkOffsetBox chunkOffsetBox : chunkOffsetBoxes) {
            long[] offsets = chunkOffsetBox.getChunkOffsets();
            for (int i = 0; i < offsets.length; i++) {
                offsets[i] += dataOffset;
            }
        }


        return isoFile;
    }

    private MovieBox createMovieBox(Movie movie) {
        MovieBox movieBox = new MovieBox();
        List<Box> movieBoxChildren = new LinkedList<Box>();
        MovieHeaderBox mvhd = new MovieHeaderBox();
        mvhd.setCreationTime(DateHelper.convert(new Date()));
        mvhd.setDuration(movie.getMovieMetaData().getDuration());
        mvhd.setModificationTime(DateHelper.convert(new Date()));
        mvhd.setDuration(movie.getMovieMetaData().getDuration());
        mvhd.setTimescale(movie.getMovieMetaData().getTimescale());
        // find the next available trackId
        long nextTrackId = 0;
        for (Track track : movie.getTracks()) {
            nextTrackId = nextTrackId < track.getTrackMetaData().getTrackId() ? track.getTrackMetaData().getTrackId() : nextTrackId;
        }
        mvhd.setNextTrackId(++nextTrackId);
        movieBoxChildren.add(mvhd);
        for (Track track : movie.getTracks()) {
            if (track.getType() != Track.Type.UNKNOWN) {
                movieBoxChildren.add(createTrackBox(track, movie));
            }
        }
        // metadata here
        movieBox.setBoxes(movieBoxChildren);
        return movieBox;

    }

    private TrackBox createTrackBox(Track track, Movie movie) {
        LOG.info("Creating Track " + track);
        TrackBox trackBox = new TrackBox();
        TrackHeaderBox tkhd = new TrackHeaderBox();
        int flags = 0;
        if (track.isEnabled()) {
            flags += 1;
        }

        if (track.isInMovie()) {
            flags += 2;
        }

        if (track.isInPreview()) {
            flags += 4;
        }

        if (track.isInPoster()) {
            flags += 8;
        }
        tkhd.setFlags(flags);

        tkhd.setAlternateGroup(0);
        tkhd.setCreationTime(DateHelper.convert(track.getTrackMetaData().getCreationTime()));
        // We need to take edit list box into account in trackheader duration
        // but as long as I don't support edit list boxes it is sufficient to
        // just translate media duration to movie timescale
        tkhd.setDuration(track.getTrackMetaData().getDuration() * movie.getMovieMetaData().getTimescale() / track.getTrackMetaData().getTimescale());
        tkhd.setHeight(track.getTrackMetaData().getHeight());
        tkhd.setWidth(track.getTrackMetaData().getWidth());
        tkhd.setLayer(track.getTrackMetaData().getLayer());
        tkhd.setModificationTime(DateHelper.convert(new Date()));
        tkhd.setTrackId(track.getTrackMetaData().getTrackId());
        tkhd.setVolume(track.getTrackMetaData().getVolume());
        trackBox.addBox(tkhd);
        MediaBox mdia = new MediaBox();
        trackBox.addBox(mdia);
        MediaHeaderBox mdhd = new MediaHeaderBox();
        mdhd.setCreationTime(DateHelper.convert(track.getTrackMetaData().getCreationTime()));
        mdhd.setDuration(track.getTrackMetaData().getDuration());
        mdhd.setTimescale(track.getTrackMetaData().getTimescale());
        mdhd.setLanguage(track.getTrackMetaData().getLanguage());
        mdia.addBox(mdhd);
        HandlerBox hdlr = new HandlerBox();
        mdia.addBox(hdlr);
        switch (track.getType()) {
            case VIDEO:
                hdlr.setHandlerType("vide");
                break;
            case SOUND:
                hdlr.setHandlerType("soun");
                break;
            case HINT:
                hdlr.setHandlerType("hint");
                break;
            default:
                throw new RuntimeException("Dont know handler type " + track.getType());
        }

        MediaInformationBox minf = new MediaInformationBox();
        switch (track.getType()) {
            case VIDEO:
                VideoMediaHeaderBox vmhd = new VideoMediaHeaderBox();
                minf.addBox(vmhd);
                break;
            case SOUND:
                SoundMediaHeaderBox smhd = new SoundMediaHeaderBox();
                minf.addBox(smhd);
                break;
            case HINT:
                HintMediaHeaderBox hmhd = new HintMediaHeaderBox();
                minf.addBox(hmhd);
                break;
            case NULL:
                NullMediaHeaderBox nmhd = new NullMediaHeaderBox();
                minf.addBox(nmhd);
                break;
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

        SampleTableBox stbl = new SampleTableBox();

        stbl.addBox(track.getSampleDescriptionBox());

        if (track.getDecodingTimeEntries() != null && !track.getDecodingTimeEntries().isEmpty()) {
            TimeToSampleBox stts = new TimeToSampleBox();
            stts.setEntries(track.getDecodingTimeEntries());
            stbl.addBox(stts);
        }
        if (track.getCompositionTimeEntries() != null && !track.getCompositionTimeEntries().isEmpty()) {
            CompositionTimeToSample ctts = new CompositionTimeToSample();
            ctts.setEntries(track.getCompositionTimeEntries());
            stbl.addBox(ctts);
        }

        if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
            SyncSampleBox stss = new SyncSampleBox();
            stss.setSampleNumber(track.getSyncSamples());
            stbl.addBox(stss);
        }
        if (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty()) {
            SampleDependencyTypeBox sdtp = new SampleDependencyTypeBox();
            sdtp.setEntries(track.getSampleDependencies());
            stbl.addBox(sdtp);
        }
        long chunkSize[] = getChunkSizes(track, movie);
        SampleToChunkBox stsc = new SampleToChunkBox();
        stsc.setEntries(new LinkedList<SampleToChunkBox.Entry>());
        long lastChunkSize = Integer.MIN_VALUE; // to be sure the first chunks hasn't got the same size
        for (int i = 0; i < chunkSize.length; i++) {
            // The sample description index references the sample description box
            // that describes the samples of this chunk. My Tracks cannot have more
            // than one sample description box. Therefore 1 is always right
            // the first chunk has the number '1'
            if (lastChunkSize != chunkSize[i]) {
                stsc.getEntries().add(new SampleToChunkBox.Entry(i + 1, chunkSize[i], 1));
                lastChunkSize = chunkSize[i];
            }
        }
        stbl.addBox(stsc);

        SampleSizeBox stsz = new SampleSizeBox();
        long[] sizes = new long[track.getSamples().size()];
        for (int i = 0; i < sizes.length; i++) {
            sizes[i] = track.getSamples().get(i).size();
        }
        stsz.setEntrySize(sizes);

        stbl.addBox(stsz);
        // The ChunkOffsetBox we create here is just a stub
        // since we haven't created the whole structure we can't tell where the
        // first chunk starts (mdat box). So I just let the chunk offset
        // start at zero and I will add the mdat offset later.
        StaticChunkOffsetBox stco = new StaticChunkOffsetBox();
        this.chunkOffsetBoxes.add(stco);
        long offset = 0;
        long[] chunkOffset = new long[chunkSize.length];
        // all tracks have the same number of chunks
        LOG.fine("Calculating chunk offsets for track_" + track.getTrackMetaData().getTrackId());
        for (int i = 0; i < chunkSize.length; i++) {
            // The filelayout will be:
            // chunk_1_track_1,... ,chunk_1_track_n, chunk_2_track_1,... ,chunk_2_track_n, ... , chunk_m_track_1,... ,chunk_m_track_n
            // calculating the offsets
            LOG.finer("Calculating chunk offsets for track_" + track.getTrackMetaData().getTrackId() + " chunk " + i);
            for (Track current : movie.getTracks()) {
                LOG.finest("Adding offsets of track_" + current.getTrackMetaData().getTrackId());
                long[] chunkSizes = getChunkSizes(current, movie);
                long firstSampleOfChunk = 0;
                for (int j = 0; j < i; j++) {
                    firstSampleOfChunk += chunkSizes[j];
                }
                if (current == track) {
                    chunkOffset[i] = offset;
                }

                for (long j = firstSampleOfChunk; j < firstSampleOfChunk + chunkSizes[i]; j++) {
                    if (j > Integer.MAX_VALUE) {
                        throw new InternalError("I cannot deal with a number of samples > Integer.MAX_VALUE");
                    }

                    offset += current.getSamples().get((int) j).size();
                }
            }
        }
        stco.setChunkOffsets(chunkOffset);
        stbl.addBox(stco);
        minf.addBox(stbl);
        mdia.addBox(minf);

        return trackBox;
    }

    private static class InterleaveChunkMdat extends AbstractBox {
        List<Track> tracks;
        Map<Track, long[]> chunks = new HashMap<Track, long[]>();

        private InterleaveChunkMdat(Movie movie) {
            super(IsoFile.fourCCtoBytes("mdat"));
            tracks = movie.getTracks();
            for (Track track : movie.getTracks()) {
                chunks.put(track, getChunkSizes(track, movie));
            }
        }

        @Override
        protected long getContentSize() {
            long size = 0;
            for (Track track : tracks) {
                for (IsoBufferWrapper sample : track.getSamples()) {
                    size += sample.size();
                }
            }
            return size;
        }

        @Override
        public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
            throw new InternalError("This box cannot be created by parsing");
        }

        @Override
        public String getDisplayName() {
            return null;
        }



        @Override
        protected void getContent(IsoOutputStream os) throws IOException {
            long aaa = 0;
            // all tracks have the same number of chunks
            for (int i = 0; i < chunks.values().iterator().next().length; i++) {
                for (Track track : chunks.keySet()) {

                    long[] chunkSizes = chunks.get(track);
                    long firstSampleOfChunk = 0;
                    for (int j = 0; j < i; j++) {
                        firstSampleOfChunk += chunkSizes[j];
                    }

                    for (long j = firstSampleOfChunk; j < firstSampleOfChunk + chunkSizes[i]; j++) {
                        if (j > Integer.MAX_VALUE) {
                            throw new InternalError("I cannot deal with a number of samples > Integer.MAX_VALUE");
                        }

                        IsoBufferWrapper ibw = track.getSamples().get((int) j);
                        while (ibw.remaining() >= 1024) {
                            os.write(ibw.read(1024));
                        }
                        while (ibw.remaining() > 0) {
                            os.write(ibw.read());
                        }

                    }

                }

            }
            System.err.println(aaa);

        }
    }

    /**
     * Gets the chunk sizes for the given track.
     *
     * @param track
     * @param movie
     * @return
     */
    static long[] getChunkSizes(Track track, Movie movie) {
        Track referenceTrack = null;
        long[] referenceChunkStarts = null;
        long referenceSampleCount = 0;
        for (Track test : movie.getTracks()) {
            if (test.getSyncSamples() != null && test.getSyncSamples().length > 0) {
                referenceTrack = test;
                referenceChunkStarts = test.getSyncSamples();
                referenceSampleCount = test.getSamples().size();
            }
        }
        if (referenceTrack == null) {
            throw new RuntimeException("need some sync samples");

        }


        long[] chunkSizes = new long[referenceTrack.getSyncSamples().length];

        long sc = track.getSamples().size();
        // Since the number of sample differs per track enormously 25 fps vs Audio for example
        // we calculate the stretch. Stretch is the number of samples in current track that
        // are needed for the time one sample in reference track is presented.
        double stretch = (double) sc / referenceSampleCount;
        for (int i = 0; i < chunkSizes.length; i++) {
            long start = Math.round(stretch * (referenceChunkStarts[i] - 1));
            long end = 0;
            if (referenceChunkStarts.length == i + 1) {
                end = Math.round(stretch * (referenceSampleCount));
            } else {
                end = Math.round(stretch * (referenceChunkStarts[i + 1] - 1));
            }

            chunkSizes[i] = end - start;
            // The Stretch makes sure that there are as much audio and video chunks!
        }
        assert track.getSamples().size() == sum(chunkSizes) : "The number of samples and the sum of all chunk lengths must be equal";
        return chunkSizes;


    }


    private static long sum(long[] ls) {
        long rc = 0;
        for (long l : ls) {
            rc += l;
        }
        return rc;
    }
}
