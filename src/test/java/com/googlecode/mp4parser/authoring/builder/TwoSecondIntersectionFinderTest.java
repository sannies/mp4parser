package com.googlecode.mp4parser.authoring.builder;

import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;

/**
 * Just check it works.
 */
public class TwoSecondIntersectionFinderTest {
    long[] samples = new long[]{1, 87, 173, 259, 345, 431, 517, 603, 690, 776, 862, 948, 1034, 1120, 1206, 1292, 1379, 1465, 1551, 1637, 1723, 1809, 1895, 1982, 2068, 2154, 2240, 2326, 2412, 2498, 2584, 2671, 2757, 2843, 2929, 3015, 3101, 3187, 3274, 3360, 3446, 3532, 3618, 3704, 3790, 3876, 3963, 4049, 4135, 4221, 4307, 4393, 4479, 4566, 4652};

    @Test
    public void testSampleNumbers() throws Exception {
        Movie m = MovieCreator.build(new FileDataSourceImpl(TwoSecondIntersectionFinderTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/Beethoven - Bagatelle op.119 no.11 i.m4a"));
        TwoSecondIntersectionFinder intersectionFinder = new TwoSecondIntersectionFinder(m, 2);
        long[] s = intersectionFinder.sampleNumbers(m.getTracks().get(0));
        Assert.assertArrayEquals(samples, s);
    }
}
