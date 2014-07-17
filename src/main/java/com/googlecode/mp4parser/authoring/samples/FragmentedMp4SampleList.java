package com.googlecode.mp4parser.authoring.samples;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.fragment.*;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.SampleImpl;
import com.googlecode.mp4parser.util.Path;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.*;

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
    private Map<TrackRunBox, SoftReference<ByteBuffer>>
            trunDataCache = new HashMap<TrackRunBox, SoftReference<ByteBuffer>>();
    private int firstSamples[];
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
        initAllFragments();
    }

    private List<TrackFragmentBox> initAllFragments() {
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
        int firstSample = 1;
        firstSamples = new int[allTrafs.size()];
        for (int i = 0; i < allTrafs.size(); i++) {
            firstSamples[i] = firstSample;
            firstSample += getTrafSize(allTrafs.get(i));
        }
        return trafs;
    }

    private int getTrafSize(TrackFragmentBox traf) {
        List<Box> boxes = traf.getBoxes();
        int size = 0;
        for (int i = 0; i < boxes.size(); i++) {
            Box b = boxes.get(i);
            if (b instanceof TrackRunBox) {
                size += l2i(((TrackRunBox) b).getSampleCount());
            }
        }
        return size;
    }

    @Override
    public Sample get(int index) {

        Sample cachedSample;
        if (sampleCache[index] != null && (cachedSample = sampleCache[index].get()) != null) {
            return cachedSample;
        }


        int targetIndex = index + 1;
        int j = firstSamples.length-1;
        while (targetIndex -  firstSamples[j] < 0) {
            j--;
        }
        TrackFragmentBox trackFragmentBox  = allTrafs.get(j);
        // we got the correct traf.
        int sampleIndexWithInTraf = targetIndex - firstSamples[j];
        MovieFragmentBox moof = ((MovieFragmentBox) trackFragmentBox.getParent());

        List<TrackRunBox> truns = trackFragmentBox.getBoxes(TrackRunBox.class);
        if (truns.size()!=1) {
            throw new RuntimeException("Please make me work with more than one trun");
        }
        TrackRunBox trun = truns.get(0);
        List<TrackRunBox.Entry> trackRunEntries = trun.getEntries();
        TrackFragmentHeaderBox tfhd = trackFragmentBox.getTrackFragmentHeaderBox();

        long offset = 0;
        Container base;
        if (tfhd.hasBaseDataOffset()) {
            offset += tfhd.getBaseDataOffset();
            base = moof.getParent();
        } else {
            base = moof;
        }

        if (trun.isDataOffsetPresent()) {
            offset += trun.getDataOffset();
        }

        boolean sampleSizePresent = trun.isSampleSizePresent();
        boolean hasDefaultSampleSize = tfhd.hasDefaultSampleSize();
        long defaultSampleSize = 0;
        if (!sampleSizePresent) {
            if (hasDefaultSampleSize) {
                defaultSampleSize  = tfhd.getDefaultSampleSize();
            } else {
                if (trex == null) {
                    throw new RuntimeException("File doesn't contain trex box but track fragments aren't fully self contained. Cannot determine sample size.");
                }
                defaultSampleSize  = trex.getDefaultSampleSize();
            }
        }
        for (int i = 0; i < sampleIndexWithInTraf; i++) {
            if (sampleSizePresent) {
                offset += trackRunEntries.get(i).getSampleSize();
            } else {
                offset += defaultSampleSize;
            }
        }
        long sampleSize;
        if (sampleSizePresent) {
            sampleSize = trackRunEntries.get(sampleIndexWithInTraf).getSampleSize();
        } else {
            sampleSize = defaultSampleSize;
        }
        final SampleImpl sample = new SampleImpl(offset, sampleSize, base);
        sampleCache[index] = new SoftReference<Sample>(sample);
        return sample;

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
