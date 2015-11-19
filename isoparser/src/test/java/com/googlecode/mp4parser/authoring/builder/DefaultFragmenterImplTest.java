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

        long[] segments = new DefaultFragmenterImpl(2).sampleNumbers(movie.getTracks().get(0));
        Assert.assertArrayEquals(new
                        long[]{1, 87, 174, 261, 348, 435, 522, 609, 696, 783, 870, 957, 1044, 1131, 1218, 1305, 1392, 1479, 1566, 1653, 1740, 1827, 1914, 2001, 2088, 2175, 2262, 2349, 2436, 2523, 2610, 2697, 2784, 2871, 2958, 3045, 3132, 3219, 3306, 3393, 3480, 3567, 3654, 3741, 3828, 3915, 4002, 4089, 4176, 4263, 4350, 4437, 4524, 4611, 4698},
                segments
        );
        long[] segments2 = new DefaultFragmenterImpl(4).sampleNumbers(movie.getTracks().get(0));
        Assert.assertArrayEquals(new
                        long[]{1, 173, 346, 519, 692, 865, 1038, 1211, 1384, 1557, 1730, 1903, 2076, 2249, 2422, 2595, 2768, 2941, 3114, 3287, 3460, 3633, 3806, 3979, 4152, 4325, 4498, 4671,},
                segments2
        );

    }

}
