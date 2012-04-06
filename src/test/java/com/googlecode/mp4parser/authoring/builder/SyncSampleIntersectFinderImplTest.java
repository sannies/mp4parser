package com.googlecode.mp4parser.authoring.builder;


import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.TestMovieCreator;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.Channels;

public class SyncSampleIntersectFinderImplTest {



    @Test
    public void testFindSameFrameRate() throws IOException {
        Movie m = TestMovieCreator.createMovieOnlyVideo(
                "/scene_cut_on/FBW_fixedres_B_640x360_1200.mp4",
                "/scene_cut_on/FBW_fixedres_B_640x360_2400.mp4"
        );

        SyncSampleIntersectFinderImpl syncSampleIntersectFinder = new SyncSampleIntersectFinderImpl();
        int[] fragmentStartSamplesRef = null;
        Assert.assertTrue(m.getTracks().size() > 1);
        for (Track track : m.getTracks()) {
            int[] fragmentStartSamples = syncSampleIntersectFinder.sampleNumbers(track, m);
            Assert.assertNotNull(fragmentStartSamples);
            if (fragmentStartSamplesRef == null) {
                fragmentStartSamplesRef = fragmentStartSamples;
            } else {
                Assert.assertArrayEquals(fragmentStartSamplesRef, fragmentStartSamples);
            }

        }
    }

    @Test
    public void testGetIndicesToBeRemoved() {
        long[] a_sample = new long[]{20, 40, 48, 60, 80, 82};
        long[] a_times = new long[]{10, 20, 24, 30, 40, 41};
        long[] b_1 = new long[]{10, 20, 26, 30, 40};
        long[] b_2 = new long[]{10, 20, 25, 30, 40};
        long[] a_2 = SyncSampleIntersectFinderImpl.getCommonIndices(a_sample, a_times, b_1, b_2);
        Assert.assertArrayEquals(new long[]{20, 40, 60, 80}, a_2);
    }

    @Test
    public void testFindDifferentFrameRates() throws IOException {

        /*Movie m = createMovieOnlyVideo(
                "/working_now/FBW_fixedres_B_640x360_200.mp4",
                "/working_now/FBW_fixedres_B_640x360_400.mp4",
                "/working_now/FBW_fixedres_B_640x360_800.mp4",
                "/working_now/FBW_fixedres_B_640x360_1200.mp4",
                "/working_now/FBW_fixedres_B_640x360_2400.mp4"
        );    */
        Movie m = TestMovieCreator.createMovieOnlyVideo(
                "/scene_cut_on/FBW_fixedres_B_640x360_200.mp4",
                "/scene_cut_on/FBW_fixedres_B_640x360_400.mp4",
                "/scene_cut_on/FBW_fixedres_B_640x360_800.mp4",
                "/scene_cut_on/FBW_fixedres_B_640x360_1200.mp4",
                "/scene_cut_on/FBW_fixedres_B_640x360_2400.mp4"
        );
        SyncSampleIntersectFinderImpl syncSampleIntersectFinder = new SyncSampleIntersectFinderImpl();
        int[] fragmentStartSamplesRef = null;
        for (Track track : m.getTracks()) {
            int[] fragmentStartSamples = syncSampleIntersectFinder.sampleNumbers(track, m);
            Assert.assertNotNull(fragmentStartSamples);
            if (fragmentStartSamplesRef == null) {
                fragmentStartSamplesRef = fragmentStartSamples;
            } else {
                Assert.assertArrayEquals(fragmentStartSamplesRef, fragmentStartSamples);
            }

        }

    }
}
