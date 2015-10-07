package org.mp4parser.muxer.samples;

import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.muxer.RandomAccessSource;
import org.mp4parser.muxer.Sample;
import org.mp4parser.tools.Offsets;
import org.mp4parser.tools.Path;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.*;

import static org.mp4parser.tools.CastUtils.l2i;

public class FragmentedMp4SampleList extends AbstractList<Sample> {
    Container isofile;

    TrackBox trackBox = null;
    TrackExtendsBox trex = null;
    HashMap<TrackFragmentBox, MovieFragmentBox> traf2moof = new HashMap<TrackFragmentBox, MovieFragmentBox>();
    private SoftReference<Sample> sampleCache[];
    private List<TrackFragmentBox> allTrafs;
    private Map<TrackRunBox, SoftReference<ByteBuffer>>
            trunDataCache = new HashMap<TrackRunBox, SoftReference<ByteBuffer>>();
    private int firstSamples[];
    private int size_ = -1;
    private RandomAccessSource randomAccess;

    public FragmentedMp4SampleList(long track, Container isofile, RandomAccessSource randomAccess) {
        this.isofile = isofile;
        this.randomAccess = randomAccess;
        List<TrackBox> tbs = Path.getPaths(isofile, "moov[0]/trak");
        for (TrackBox tb : tbs) {
            if (tb.getTrackHeaderBox().getTrackId() == track) {
                trackBox = tb;
            }
        }
        if (trackBox == null) {
            throw new RuntimeException("This MP4 does not contain track " + track);
        }

        List<TrackExtendsBox> trexs = Path.getPaths(isofile, "moov[0]/mvex[0]/trex");
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
        for (MovieFragmentBox moof : isofile.getBoxes(MovieFragmentBox.class)) {
            for (TrackFragmentBox trackFragmentBox : moof.getBoxes(TrackFragmentBox.class)) {
                if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                    trafs.add(trackFragmentBox);
                    traf2moof.put(trackFragmentBox, moof);
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
        for (Box b : boxes) {
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
        MovieFragmentBox moof = traf2moof.get(trackFragmentBox);

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

                        if (tfhd.hasBaseDataOffset()) {
                            offset += tfhd.getBaseDataOffset();
                        } else {
                            if (tfhd.isDefaultBaseIsMoof()) {
                                offset += Offsets.find(isofile, moof, 0);
                            } else {
                                throw new RuntimeException("Rethink this case");
                            }
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
                            trunData = randomAccess.get(offset, size);
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
                        sampleSize = trackRunEntries.get(sampleIndexWithInTraf - previousTrunsSize).getSampleSize();
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
                            return (ByteBuffer) ((ByteBuffer) finalTrunData.position(finalOffset)).slice().limit(l2i(sampleSize));
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
        for (MovieFragmentBox moof : isofile.getBoxes(MovieFragmentBox.class)) {
            for (TrackFragmentBox trackFragmentBox : moof.getBoxes(TrackFragmentBox.class)) {
                if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                    for (TrackRunBox trackRunBox : trackFragmentBox.getBoxes(TrackRunBox.class)) {
                        i += trackRunBox.getSampleCount();
                    }

                }
            }
        }

        size_ = i;
        return i;
    }


}
