package org.mp4parser.muxer.samples;

import org.mp4parser.Container;
import org.mp4parser.muxer.RandomAccessSource;
import org.mp4parser.muxer.Sample;
import org.mp4parser.tools.Path;

import java.util.AbstractList;
import java.util.List;

/**
 * Creates a list of <code>ByteBuffer</code>s that represent the samples of a given track.
 */
public class SampleList extends AbstractList<Sample> {
    List<Sample> samples;


    public SampleList(long trackId, Container isofile, RandomAccessSource source) {

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
