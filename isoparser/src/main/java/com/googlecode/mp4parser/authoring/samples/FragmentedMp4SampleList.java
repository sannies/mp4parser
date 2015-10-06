package com.googlecode.mp4parser.authoring.samples;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.fragment.*;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.util.Path;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
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
        List<TrackBox> tbs = Path.getPaths(topLevel, "moov[0]/trak");
        for (TrackBox tb : tbs) {
            if (tb.getTrackHeaderBox().getTrackId() == track) {
                trackBox = tb;
            }
        }
        if (trackBox == null) {
            throw new RuntimeException("This MP4 does not contain track " + track);
        }

        List<TrackExtendsBox> trexs = Path.getPaths(topLevel, "moov[0]/mvex[0]/trex");
        for (TrackExtendsBox box : trexs) {
            if (box.getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                trex = box;
            }
        }
        sampleCache = (SoftReference<Sample>[]) Array.newInstance(SoftReference.class, size());
        initAllFragments();
    }

    private List<TrackFragmentBox> initAllFragments() {
        if (allTrafs != null) {
            return allTrafs;
        }
        List<TrackFragmentBox> trafs = new ArrayList<TrackFragmentBox>();
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
        int j = firstSamples.length - 1;
        while (targetIndex - firstSamples[j] < 0) {
            j--;
        }
        TrackFragmentBox trackFragmentBox = allTrafs.get(j);
        // we got the correct traf.
        int sampleIndexWithInTraf = targetIndex - firstSamples[j];
        int previousTrunsSize = 0;
        MovieFragmentBox moof = ((MovieFragmentBox) trackFragmentBox.getParent());

        for (Box box : trackFragmentBox.getBoxes()) {
            if (box instanceof TrackRunBox) {
                TrackRunBox trun = (TrackRunBox) box;


                if (trun.getEntries().size() <= (sampleIndexWithInTraf - previousTrunsSize)) {
                    previousTrunsSize += trun.getEntries().size();
                } else {
                    // we are in correct trun box


                    List<TrackRunBox.Entry> trackRunEntries = trun.getEntries();
                    TrackFragmentHeaderBox tfhd = trackFragmentBox.getTrackFragmentHeaderBox();
                    boolean sampleSizePresent = trun.isSampleSizePresent();
                    boolean hasDefaultSampleSize = tfhd.hasDefaultSampleSize();
                    long defaultSampleSize = 0;
                    if (!sampleSizePresent) {
                        if (hasDefaultSampleSize) {
                            defaultSampleSize = tfhd.getDefaultSampleSize();
                        } else {
                            if (trex == null) {
                                throw new RuntimeException("File doesn't contain trex box but track fragments aren't fully self contained. Cannot determine sample size.");
                            }
                            defaultSampleSize = trex.getDefaultSampleSize();
                        }
                    }

                    final SoftReference<ByteBuffer> trunDataRef = trunDataCache.get(trun);
                    ByteBuffer trunData = trunDataRef != null ? trunDataRef.get() : null;
                    if (trunData == null) {
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
                        int size = 0;
                        for (TrackRunBox.Entry e : trackRunEntries) {
                            if (sampleSizePresent) {
                                size += e.getSampleSize();
                            } else {
                                size += defaultSampleSize;
                            }
                        }
                        try {
                            trunData = base.getByteBuffer(offset, size);
                            trunDataCache.put(trun, new SoftReference<ByteBuffer>(trunData));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }

                    int offset = 0;
                    for (int i = 0; i < (sampleIndexWithInTraf - previousTrunsSize); i++) {
                        if (sampleSizePresent) {
                            offset += trackRunEntries.get(i).getSampleSize();
                        } else {
                            offset += defaultSampleSize;
                        }
                    }
                    final long sampleSize;
                    if (sampleSizePresent) {
                        sampleSize = trackRunEntries.get(sampleIndexWithInTraf- previousTrunsSize).getSampleSize();
                    } else {
                        sampleSize = defaultSampleSize;
                    }

                    final ByteBuffer finalTrunData = trunData;
                    final int finalOffset = offset;
                    Sample sample = new Sample() {

                        public void writeTo(WritableByteChannel channel) throws IOException {
                            channel.write(asByteBuffer());
                        }

                        public long getSize() {
                            return sampleSize;
                        }

                        public ByteBuffer asByteBuffer() {
                            return (ByteBuffer) ((ByteBuffer)finalTrunData.position(finalOffset)).slice().limit(l2i(sampleSize));
                        }
                    };
                    sampleCache[index] = new SoftReference<Sample>(sample);
                    return sample;
                }
            }
        }

        throw new RuntimeException("Couldn't find sample in the traf I was looking");
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
                    for (TrackRunBox trackRunBox : trackFragmentBox.getBoxes(TrackRunBox.class)) {
                        i += trackRunBox.getSampleCount();
                    }

                }
            }
        }
        for (IsoFile fragment : fragments) {
            for (MovieFragmentBox moof : fragment.getBoxes(MovieFragmentBox.class)) {
                for (TrackFragmentBox trackFragmentBox : moof.getBoxes(TrackFragmentBox.class)) {
                    if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                        for (TrackRunBox trackRunBox : trackFragmentBox.getBoxes(TrackRunBox.class)) {
                            i += trackRunBox.getSampleCount();
                        }
                    }
                }
            }
        }
        size_ = i;
        return i;
    }


}
