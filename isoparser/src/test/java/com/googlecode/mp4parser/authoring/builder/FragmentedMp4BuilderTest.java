package com.googlecode.mp4parser.authoring.builder;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.junit.Test;

import java.io.FileOutputStream;

import static org.junit.Assert.*;

/**
 * Created by sannies on 04.09.2015.
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
        fragmentedMp4Builder.setIntersectionFinder(new TimeBasedFragmenter(m, 5));

        Container c = fragmentedMp4Builder.build(m);
        // c.writeContainer(new FileOutputStream("output.mp4").getChannel());

    }
}