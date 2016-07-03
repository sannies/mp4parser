package com.googlecode.mp4parser.authoring.builder;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class DefaultFragmenterImplTest {

    /**
     * This test indicated that you changed the output. Do you expect that?
     */
    @Test
    public void stabilize() throws IOException, NoSuchAlgorithmException {
        Movie movie = MovieCreator.build(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile() +
                "/Beethoven - Bagatelle op.119 no.11 i.m4a");

        long[] segments = new BetterFragmenter(2).sampleNumbers(movie.getTracks().get(0));

        Assert.assertArrayEquals(new
                        long[]{1, 88, 175, 262, 349, 436, 523, 610, 697, 784, 871, 958, 1045, 1132, 1219, 1306, 1393, 1480, 1567, 1654, 1741, 1828, 1915, 2002, 2089, 2176, 2263, 2350, 2437, 2524, 2611, 2698, 2785, 2872, 2959, 3046, 3133, 3220, 3307, 3394, 3481, 3568, 3655, 3742, 3829, 3916, 4003, 4090, 4177, 4264, 4351, 4438, 4525, 4612, 4699, 4775,},
                segments
        );
        long[] segments2 = new BetterFragmenter(4).sampleNumbers(movie.getTracks().get(0));
        Assert.assertArrayEquals(new
                        long[]{1, 174, 347, 520, 693, 866, 1039, 1212, 1385, 1558, 1731, 1904, 2077, 2250, 2423, 2596, 2769, 2942, 3115, 3288, 3461, 3634, 3807, 3980, 4153, 4326, 4499, 4672, 4762,},
                segments2
        );

    }

}
