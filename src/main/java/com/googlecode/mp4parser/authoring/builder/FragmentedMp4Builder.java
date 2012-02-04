package com.googlecode.mp4parser.authoring.builder;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
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
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.SampleToChunkBox;
import com.coremedia.iso.boxes.SoundMediaHeaderBox;
import com.coremedia.iso.boxes.StaticChunkOffsetBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.coremedia.iso.boxes.VideoMediaHeaderBox;
import com.coremedia.iso.boxes.WriteListener;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentHeaderBox;
import com.coremedia.iso.boxes.fragment.SampleFlags;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentHeaderBox;
import com.coremedia.iso.boxes.fragment.TrackRunBox;
import com.coremedia.iso.boxes.mdat.MediaDataBoxWithSamples;
import com.googlecode.mp4parser.authoring.DateHelper;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * Creates a fragmented MP4 file.
 */
public class FragmentedMp4Builder implements Mp4Builder {
    FragmentIntersectionFinder intersectionFinder;
    private static final Logger LOG = Logger.getLogger(FragmentedMp4Builder.class.getName());

    public IsoFile build(Movie movie) throws IOException {
        LOG.info("Creating movie " + movie);
        IsoFile isoFile = new IsoFile(new IsoBufferWrapperImpl(new byte[]{}));
        isoFile.parse();
        // ouch that is ugly but I don't know how to do it else
        List<String> minorBrands = new LinkedList<String>();
        minorBrands.add("isom");
        minorBrands.add("iso2");
        minorBrands.add("avc1");

        isoFile.addBox(new FileTypeBox("isom", 0, minorBrands));
        isoFile.addBox(createMovieBox(movie));


        int maxNumberOfFragments = 0;
        for (Track track : movie.getTracks()) {
            int currentLength = intersectionFinder.sampleNumbers(track, movie).length;
            maxNumberOfFragments = currentLength > maxNumberOfFragments ? currentLength : maxNumberOfFragments;
        }

        for (int i = 0; i < maxNumberOfFragments; i++) {
            for (Track track : movie.getTracks()) {
                int[] startSamples = intersectionFinder.sampleNumbers(track, movie);
                if (i < startSamples.length) {
                    int startSample = startSamples[i];

                    int endSample = i + 1 < startSamples.length ? startSamples[i + 1] : track.getSamples().size();

                    if (startSample == endSample) {
                        // empty fragment
                        // just don't add any boxes.
                    } else {
                        isoFile.addBox(createMoof(startSample, endSample, track, i));
                        isoFile.addBox(new MediaDataBoxWithSamples(track.getSamples().subList(startSample, endSample)));
                    }

                } else {
                    //obvious this track has not that many fragments
                }
            }
        }


        return isoFile;
    }

    private MovieFragmentBox createMoof(int startSample, int endSample, Track track, int sequenceNumber) {
        List<IsoBufferWrapper> samples = track.getSamples().subList(startSample, endSample);

        long[] sampleSizes = new long[samples.size()];
        for (int i = 0; i < sampleSizes.length; i++) {
            sampleSizes[i] = samples.get(i).size();

        }


        final TrackFragmentHeaderBox tfhd = new TrackFragmentHeaderBox();
        tfhd.setBaseDataOffset(-1);
        SampleFlags sf = new SampleFlags(0x0000c0);

        tfhd.setDefaultSampleFlags(sf);
        MovieFragmentBox moof = new MovieFragmentBox();
        moof.addWriteListener(new WriteListener() {
            public void beforeWrite(long offset) {
                tfhd.setBaseDataOffset(offset);
            }
        });

        MovieFragmentHeaderBox mfhd = new MovieFragmentHeaderBox();
        moof.addBox(mfhd);
        TrackFragmentBox traf = new TrackFragmentBox();
        moof.addBox(traf);

        traf.addBox(tfhd);
        TrackRunBox trun = new TrackRunBox();
        traf.addBox(trun);

        mfhd.setSequenceNumber(sequenceNumber);
        tfhd.setTrackId(track.getTrackMetaData().getTrackId());

        trun.setFlags(0x100 | 0x200 | 0x001 | 0x800 | 0x004);
        trun.setFirstSampleFlags(new SampleFlags(0x00000040));
        // sampleDuration | sampleSize | dataOffset | compositionTime | firstSampleFlags

        List<TrackRunBox.Entry> entries = new ArrayList<TrackRunBox.Entry>(endSample - startSample);


        int mdatSize = 0;

        Queue<TimeToSampleBox.Entry> timeQueue = new LinkedList<TimeToSampleBox.Entry>(track.getDecodingTimeEntries());
        long durationEntriesLeft = timeQueue.peek().getCount();


        Queue<CompositionTimeToSample.Entry> compositionTimeQueue =
                track.getCompositionTimeEntries() != null && track.getCompositionTimeEntries().size() > 0 ?
                        new LinkedList<CompositionTimeToSample.Entry>(track.getCompositionTimeEntries()) : null;
        long compositionTimeEntriesLeft = compositionTimeQueue != null ? compositionTimeQueue.peek().getCount() : -1;


        if (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty() ||
                track.getSyncSamples() != null && track.getSyncSamples().length != 0) {
            trun.setFlags(trun.getFlags() | 0x400);
        }

        for (int i = 0; i < sampleSizes.length; i++) {
            TrackRunBox.Entry entry = new TrackRunBox.Entry();
            entry.setSampleSize(sampleSizes[i]);
            mdatSize += sampleSizes[i];
            if (trun.isSampleFlagsPresentPresent()) {
                long flag = 0;
                if (track.getSampleDependencies() != null && !track.getSampleDependencies().isEmpty()) {

                    SampleDependencyTypeBox.Entry e = track.getSampleDependencies().get(i);
                    flag |= ((e.getSampleDependsOn() & 3) << 24);
                    flag |= ((e.getSampleIsDependentOn() & 3) << 22);
                    flag |= ((e.getSampleHasRedundancy() & 3) << 20);
                }
                if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                    if (Arrays.binarySearch(track.getSyncSamples(), i) < 0) {
                        // we have to mark non-sync samples!
                        flag |= (1 << 16);
                    }
                }
                // i don't have sample degradation
                entry.setSampleFlags(new SampleFlags(flag));

            }

            entry.setSampleDuration(timeQueue.peek().getDelta());
            if (--durationEntriesLeft == 0 && timeQueue.size() > 1) {
                timeQueue.remove();
                durationEntriesLeft = timeQueue.peek().getCount();
            }

            if (compositionTimeQueue != null) {
                entry.setSampleCompositionTimeOffset(compositionTimeQueue.peek().getOffset());
                if (--compositionTimeEntriesLeft == 0 && compositionTimeQueue.size() > 1) {
                    compositionTimeQueue.remove();
                    compositionTimeEntriesLeft = compositionTimeQueue.element().getCount();
                }
            }


            entries.add(entry);
        }
        //System.err.println(endSample - startSample);
        //System.err.println("entries.size() " + entries.size());

        trun.setEntries(entries);

        trun.setDataOffset((int) (8 + moof.getSize())); // mdat header + moof size
        return moof;
    }

    private MovieBox createMovieBox(Movie movie) {
        MovieBox movieBox = new MovieBox();
        MovieHeaderBox mvhd = new MovieHeaderBox();
        mvhd.setCreationTime(DateHelper.convert(new Date()));
        mvhd.setModificationTime(DateHelper.convert(new Date()));

        long movieTimeScale = getTimescale(movie);
        long duration = 0;

        for (Track track : movie.getTracks()) {
            long tracksDuration = getDuration(track) * movieTimeScale / track.getTrackMetaData().getTimescale();
            if (tracksDuration > duration) {
                duration = tracksDuration;
            }


        }

        mvhd.setDuration(duration);
        mvhd.setTimescale(movieTimeScale);
        // find the next available trackId
        long nextTrackId = 0;
        for (Track track : movie.getTracks()) {
            nextTrackId = nextTrackId < track.getTrackMetaData().getTrackId() ? track.getTrackMetaData().getTrackId() : nextTrackId;
        }
        mvhd.setNextTrackId(++nextTrackId);
        movieBox.addBox(mvhd);

        MovieExtendsBox mvex = new MovieExtendsBox();

        for (Track track : movie.getTracks()) {
            // Remove all boxes except the SampleDescriptionBox.

            TrackExtendsBox trex = new TrackExtendsBox();
            trex.setTrackId(track.getTrackMetaData().getTrackId());
            trex.setDefaultSampleDescriptionIndex(1);
            trex.setDefaultSampleDuration(0);
            trex.setDefaultSampleSize(0);
            trex.setDefaultSampleFlags(new SampleFlags(0));
            // Don't set any good defaults here.
            mvex.addBox(trex);
        }

        movieBox.addBox(mvex);


        for (Track track : movie.getTracks()) {
            if (track.getType() != Track.Type.UNKNOWN) {
                movieBox.addBox(createTrackBox(track, movie));
            }
        }
        // metadata here
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

        tkhd.setAlternateGroup(track.getTrackMetaData().getGroup());
        tkhd.setCreationTime(DateHelper.convert(track.getTrackMetaData().getCreationTime()));
        // We need to take edit list box into account in trackheader duration
        // but as long as I don't support edit list boxes it is sufficient to
        // just translate media duration to movie timescale
        tkhd.setDuration(getDuration(track) * getTimescale(movie) / track.getTrackMetaData().getTimescale());
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
        mdhd.setDuration(getDuration(track));
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
            case TEXT:
                hdlr.setHandlerType("text");
                break;
            case AMF0:
                hdlr.setHandlerType("data");
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
            case TEXT:
            case AMF0:
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
        stbl.addBox(new TimeToSampleBox());
        stbl.addBox(new SampleToChunkBox());
        stbl.addBox(new StaticChunkOffsetBox());

        minf.addBox(stbl);
        mdia.addBox(minf);

        return trackBox;
    }

    public void setIntersectionFinder(FragmentIntersectionFinder intersectionFinder) {
        this.intersectionFinder = intersectionFinder;
    }

    protected long getDuration(Track track) {
        long duration = 0;
        for (TimeToSampleBox.Entry entry : track.getDecodingTimeEntries()) {
            duration += entry.getCount() * entry.getDelta();
        }
        return duration;
    }

    public long getTimescale(Movie movie) {
        long timescale = movie.getTracks().iterator().next().getTrackMetaData().getTimescale();
        for (Track track : movie.getTracks()) {
            timescale = gcd(track.getTrackMetaData().getTimescale(), timescale);
        }
        return timescale;
    }

    public static long gcd(long a, long b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

}
