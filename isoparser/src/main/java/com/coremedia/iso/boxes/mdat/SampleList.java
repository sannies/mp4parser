package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.samples.DefaultMp4SampleList;
import com.googlecode.mp4parser.authoring.samples.FragmentedMp4SampleList;
import com.mp4parser.tools.Path;
import com.mp4parser.RandomAccessSource;

import java.io.IOException;
import java.util.AbstractList;
import java.util.List;

/**
 * Creates a list of <code>ByteBuffer</code>s that represent the samples of a given track.
 */
public class SampleList extends AbstractList<Sample> {
    List<Sample> samples;


    public SampleList(long trackId, Container isofile, RandomAccessSource source) throws IOException {

        if (Path.getPaths(isofile, "moov/mvex/trex").isEmpty()) {
            samples = new DefaultMp4SampleList(trackId, isofile, source);
        } else {
            samples = new FragmentedMp4SampleList(trackId, isofile, source);
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
