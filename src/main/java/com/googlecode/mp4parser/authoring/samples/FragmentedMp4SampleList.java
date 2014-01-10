package com.googlecode.mp4parser.authoring.samples;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackRunBox;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.SampleImpl;
import com.googlecode.mp4parser.util.Path;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Created by sannies on 25.05.13.
 */
public class FragmentedMp4SampleList extends AbstractList<Sample> {
    Container topLevel;
    IsoFile[] fragments;
    TrackBox trackBox = null;
    TrackExtendsBox trex = null;
    private SoftReference<Sample> sampleCache[];
    private List<TrackFragmentBox> allTrafs;
    private int size_ = -1;

    public FragmentedMp4SampleList(long track, Container topLevel, IsoFile... fragments) {
        this.topLevel = topLevel;
        this.fragments = fragments;
        List<TrackBox> trackBoxes = topLevel.getBoxes(MovieBox.class).get(0).getBoxes(TrackBox.class);

        for (TrackBox tb : trackBoxes) {
            if (tb.getTrackHeaderBox().getTrackId() == track) {
                trackBox = tb;
            }
        }
        if (trackBox == null) {
            throw new RuntimeException("This MP4 does not contain track " + track);
        }

        for (Box box : Path.getPaths(topLevel, "moov/mvex/trex")) {
            if (((TrackExtendsBox) box).getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                trex = ((TrackExtendsBox) box);
            }
        }
        sampleCache = (SoftReference<Sample>[]) Array.newInstance(SoftReference.class, size());
    }

    private List<TrackFragmentBox> allFragments() {
        if (allTrafs != null) {
            return allTrafs;
        }
        List<TrackFragmentBox> trafs = new LinkedList<TrackFragmentBox>();
        for (MovieFragmentBox moof : topLevel.getBoxes(MovieFragmentBox.class)) {
            for (TrackFragmentBox trackFragmentBox : moof.getBoxes(TrackFragmentBox.class)) {
                if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                    trafs.add(trackFragmentBox);
                }
            }
        }
        if (fragments != null) {
            for (IsoFile fragment : fragments) {
                for (MovieFragmentBox moof : fragment.getBoxes(MovieFragmentBox.class)) {
                    for (TrackFragmentBox trackFragmentBox : moof.getBoxes(TrackFragmentBox.class)) {
                        if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                            trafs.add(trackFragmentBox);
                        }
                    }
                }
            }
        }
        allTrafs = trafs;
        return trafs;
    }

    private int getTrafSize(TrackFragmentBox traf) {
        return l2i(traf.getBoxes(TrackRunBox.class).get(0).getSampleCount());
    }

    @Override
    public Sample get(int index) {

        Sample cachedSample;
        if (sampleCache[index] != null && (cachedSample = sampleCache[index].get()) != null) {
            return cachedSample;
        }

        int currentIndex = 1;
        int targetIndex = index + 1;
        for (TrackFragmentBox trackFragmentBox : allFragments()) {
            int trafSize = getTrafSize(trackFragmentBox);
            if (targetIndex >= currentIndex && targetIndex < currentIndex + trafSize) {
                // we got the correct traf.
                int sampleIndexWithInTraf = targetIndex - currentIndex;
                MovieFragmentBox moof = ((MovieFragmentBox) trackFragmentBox.getParent());
                TrackRunBox trun = trackFragmentBox.getBoxes(TrackRunBox.class).get(0);
                long offset = 0;
                if (trun.isDataOffsetPresent()) {
                    offset += trun.getDataOffset();
                }
                List<TrackRunBox.Entry> trackRunEntries = trun.getEntries();
                if (trackFragmentBox.getTrackFragmentHeaderBox().hasBaseDataOffset()) {
                    offset += trackFragmentBox.getTrackFragmentHeaderBox().getBaseDataOffset();
                } else {
                    offset += moof.getOffset();
                }


                for (int i = 0; i < sampleIndexWithInTraf; i++) {
                    if (trun.isSampleSizePresent()) {
                        offset += trackRunEntries.get(i).getSampleSize();
                    } else {
                        if (trackFragmentBox.getTrackFragmentHeaderBox().hasDefaultSampleSize()) {
                            offset += trackFragmentBox.getTrackFragmentHeaderBox().getDefaultSampleSize();
                        } else {
                            if (trex == null) {
                                throw new RuntimeException("File doesn't contain trex box but track fragments aren't fully self contained. Cannot determine sample size.");
                            }
                            offset += trex.getDefaultSampleSize();
                        }
                    }
                }
                long sampleSize;
                if (trun.isSampleSizePresent()) {
                    sampleSize = trackRunEntries.get(sampleIndexWithInTraf).getSampleSize();
                } else {
                    if (trackFragmentBox.getTrackFragmentHeaderBox().hasDefaultSampleSize()) {
                        sampleSize = trackFragmentBox.getTrackFragmentHeaderBox().getDefaultSampleSize();
                    } else {
                        if (trex == null) {
                            throw new RuntimeException("File doesn't contain trex box but track fragments aren't fully self contained. Cannot determine sample size.");
                        }
                        sampleSize = trex.getDefaultSampleSize();
                    }
                }
                final SampleImpl sample = new SampleImpl(offset, sampleSize, moof.getParent());
                sampleCache[index] = new SoftReference<Sample>(sample);
                return sample;
            }
            currentIndex += trafSize;
        }
        return null;
    }

    @Override
    public int size() {
        if (size_ != -1) {
            return size_;
        }
        int i = 0;
        for (MovieFragmentBox moof : topLevel.getBoxes(MovieFragmentBox.class)) {
            for (TrackFragmentBox trackFragmentBox : moof.getBoxes(TrackFragmentBox.class)) {
                if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                    i += trackFragmentBox.getBoxes(TrackRunBox.class).get(0).getSampleCount();
                }
            }
        }
        for (IsoFile fragment : fragments) {
            for (MovieFragmentBox moof : fragment.getBoxes(MovieFragmentBox.class)) {
                for (TrackFragmentBox trackFragmentBox : moof.getBoxes(TrackFragmentBox.class)) {
                    if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                        i += trackFragmentBox.getBoxes(TrackRunBox.class).get(0).getSampleCount();
                    }
                }
            }
        }
        size_ = i;
        return i;
    }


}
