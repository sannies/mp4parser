package org.mp4parser.muxer.container.mp4;

import org.mp4parser.Container;
import org.mp4parser.muxer.RandomAccessSource;
import org.mp4parser.muxer.Sample;
import org.mp4parser.tools.Path;

import java.util.AbstractList;
import java.util.List;

/**
 * Creates a list of <code>ByteBuffer</code>s that represent the samples of a given track.
 */
public class Mp4SampleList extends AbstractList<Sample> {
    private List<Sample> samples;


    public Mp4SampleList(long trackId, Container isofile, RandomAccessSource source) {

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
