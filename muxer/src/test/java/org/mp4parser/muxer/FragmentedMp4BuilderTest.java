package org.mp4parser.muxer;

import org.junit.Test;
import org.mp4parser.Container;
import org.mp4parser.muxer.builder.FragmentedMp4Builder;
import org.mp4parser.muxer.builder.TimeBasedFragmenter;
import org.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;

/**
 * Not really a test but at least makes sure muxing kind of works
 */
public class FragmentedMp4BuilderTest {
    @Test
    public void testSimpleMuxing() throws Exception {
        Movie m = new Movie();
        Movie v = MovieCreator.build(FragmentedMp4BuilderTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4");
        Movie a = MovieCreator.build(FragmentedMp4BuilderTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/BBB_qpfile_10sec/output_audio-2ch-20s.mp4");

        m.addTrack(v.getTracks().get(0));
        m.addTrack(a.getTracks().get(0));

        FragmentedMp4Builder fragmentedMp4Builder = new FragmentedMp4Builder();
        fragmentedMp4Builder.setFragmenter(new TimeBasedFragmenter(5));

        Container c = fragmentedMp4Builder.build(m);
        c.writeContainer(Channels.newChannel(new ByteArrayOutputStream()));

    }
}