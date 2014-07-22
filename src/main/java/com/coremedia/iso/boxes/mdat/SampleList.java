package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.samples.DefaultMp4SampleList;
import com.googlecode.mp4parser.authoring.samples.FragmentedMp4SampleList;

import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.List;

/**
 * Creates a list of <code>ByteBuffer</code>s that represent the samples of a given track.
 */
public class SampleList extends AbstractList<Sample> {
    List<Sample> samples;



    public SampleList(TrackBox trackBox, IsoFile... additionalFragments) {
        Container topLevel = ((Box) trackBox.getParent()).getParent();

        if (trackBox.getParent().getBoxes(MovieExtendsBox.class).isEmpty()) {
            if (additionalFragments.length > 0) {
                throw new RuntimeException("The TrackBox comes from a standard MP4 file. Only use the additionalFragments param if you are dealing with ( fragmented MP4 files AND additional fragments in standalone files )");
            }
            samples = new DefaultMp4SampleList(trackBox.getTrackHeaderBox().getTrackId(), topLevel);
        } else {
            samples = new FragmentedMp4SampleList(trackBox.getTrackHeaderBox().getTrackId(), topLevel, additionalFragments);
        }
    }

    @Override
    public Sample get(int index) {
        return samples.get(index);
    }

    @Override
    public int size() {
        return samples.size();
    }

}
