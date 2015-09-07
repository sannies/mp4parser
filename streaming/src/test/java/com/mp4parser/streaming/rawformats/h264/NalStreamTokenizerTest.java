package com.mp4parser.streaming.rawformats.h264;

import com.mp4parser.tools.Hex;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by sannies on 15.08.2015.
 */
public class NalStreamTokenizerTest {
    @Test
    public void testTokenize() throws Exception {
        H264AnnexBTrack.NalStreamTokenizer nst = new H264AnnexBTrack.NalStreamTokenizer(
                NalStreamTokenizerTest.class.getResourceAsStream("/com/mp4parser/streaming/rawformats/h264/tos.h264"),
                new byte[]{0, 0, 1},
                new byte[]{0, 0, 0}

        );

        byte[] nal;

        int i = 0;
        while ((nal = nst.getNext()) != null) {
            /// System.err.println(Hex.encodeHex(nal));
            i++;
        }
        Assert.assertEquals(1019, i);
        // not much of a test but hey ... better than nothing
    }
}