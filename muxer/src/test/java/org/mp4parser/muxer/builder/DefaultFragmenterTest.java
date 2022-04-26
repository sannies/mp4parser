package org.mp4parser.muxer.builder;

import org.junit.Assert;
import org.junit.Test;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.container.mp4.MovieCreator;

/**
 * Just check it works.
 */
public class DefaultFragmenterTest {
    long[] samples = new long[] { 1, 87, 174, 261, 348, 435, 522, 609, 696, 783, 870, 957, 1044, 1131, 1218, 1305, 1392, 1479, 1566, 1653, 1740, 1827, 1914,
            2001, 2088, 2175, 2262, 2349, 2436, 2523, 2610, 2697, 2784, 2871, 2958, 3045, 3132, 3219, 3306, 3393, 3480, 3567, 3654, 3741, 3828, 3915, 4002,
            4089, 4176, 4263, 4350, 4437, 4524, 4611, 4698, 4785 };

    @Test
    public void testSampleNumbers() throws Exception {
        String f = DefaultFragmenterTest.class.getClassLoader().getResource("Beethoven - Bagatelle op.119 no.11 i.m4a").toURI().getPath();
        Movie m = MovieCreator.build(f);
        DefaultFragmenterImpl intersectionFinder = new DefaultFragmenterImpl(2);
        long[] s = intersectionFinder.sampleNumbers(m.getTracks().get(0));
        String sss = "";
        for (long l : s) {
            sss += l + ", ";
        }
        System.err.println(sss);
        Assert.assertArrayEquals(samples, s);
    }
}
