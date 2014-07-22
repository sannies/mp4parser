package com.googlecode.mp4parser.authoring.builder;


import com.googlecode.mp4parser.authoring.InTestMovieCreator;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class SyncSampleIntersectFinderImplTest {



    @Test
    public void testFindSameFrameRate() throws IOException {
        Movie m = InTestMovieCreator.createMovieOnlyVideo(
                "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_150.mp4",
                "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_200.mp4"
        );

        SyncSampleIntersectFinderImpl syncSampleIntersectFinder = new SyncSampleIntersectFinderImpl(m, null, -1);
        long[] fragmentStartSamplesRef = null;
        Assert.assertTrue(m.getTracks().size() > 1);
        for (Track track : m.getTracks()) {
            long[] fragmentStartSamples = syncSampleIntersectFinder.sampleNumbers(track);
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
        SyncSampleIntersectFinderImpl syncSampleIntersectFinder = new SyncSampleIntersectFinderImpl(null,null,-1);
        long[] a_sample = new long[]{20, 40, 48, 60, 80, 82};
        long[] a_times = new long[]{10, 20, 24, 30, 40, 41};
        long[] b_1 = new long[]{10, 20, 26, 30, 40};
        long[] b_2 = new long[]{10, 20, 25, 30, 40};
        long[] a_2 = syncSampleIntersectFinder.getCommonIndices(a_sample, a_times, 10, b_1, b_2);
//        long[] a_sample = new long[]{20, 40, 48, 60, 80, 82, 100};
//        long[] a_times = new long[]{10, 20, 24, 30, 40, 41, 80, 81};
//        long[] b_1 = new long[]{10, 20, 26, 30, 40, 80};
//        long[] b_2 = new long[]{10, 20, 25, 30, 40, 80};
//        long[] a_2 = SyncSampleIntersectFinderImpl.getCommonIndices(a_sample, a_times, 10, b_1, b_2);
        Assert.assertArrayEquals(new long[]{20, 40, 60, 80}, a_2);
    }

    @Test
    public void testGetIndicesToBeRemovedMinTwoSecondsFragments() {
        SyncSampleIntersectFinderImpl syncSampleIntersectFinder = new SyncSampleIntersectFinderImpl(null,null,2);
        long[] a_sample = new long[]{20, 40, 48, 60, 80, 82, 90, 100};
        long[] a_times = new long[]{10, 20, 24, 30, 60, 61, 80, 81};
        long[] b_1 = new long[]{10, 20, 26, 30, 40, 80, 81, 100};
        long[] b_2 = new long[]{10, 20, 25, 30, 40, 80, 90, 100};
        long[] a_2 = syncSampleIntersectFinder.getCommonIndices(a_sample, a_times, 10, b_1, b_2);
        Assert.assertArrayEquals(new long[]{20, 60, 90}, a_2);
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
        Movie m = InTestMovieCreator.createMovieOnlyVideo(
                "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4",
                "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_100.mp4",
                "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_120.mp4",
                "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_150.mp4",
                "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_200.mp4"
        );
        SyncSampleIntersectFinderImpl syncSampleIntersectFinder = new SyncSampleIntersectFinderImpl(m, null, -1);
        long[] fragmentStartSamplesRef = null;
        for (Track track : m.getTracks()) {
            long[] fragmentStartSamples = syncSampleIntersectFinder.sampleNumbers(track);
            Assert.assertNotNull(fragmentStartSamples);
            if (fragmentStartSamplesRef == null) {
                fragmentStartSamplesRef = fragmentStartSamples;
            } else {
                // this is all I can do here now.
                // we should verify that all samples in the array are at the same times.
                Assert.assertEquals(fragmentStartSamplesRef.length, fragmentStartSamples.length);
            }

        }

    }
}
