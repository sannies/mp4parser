package org.mp4parser.streaming.input.h264;

import org.junit.Assert;
import org.junit.Test;
import org.mp4parser.tools.Hex;

/**
 * Created by sannies on 15.08.2015.
 */
public class NalStreamTokenizerTest {
    @Test
    public void testTokenize() throws Exception {
        H264AnnexBTrack.NalStreamTokenizer nst = new H264AnnexBTrack.NalStreamTokenizer(
                NalStreamTokenizerTest.class.getResourceAsStream("/org/mp4parser/streaming/input/h264/tos.h264")


        );

        byte[] nal;

        int i = 0;
        while ((nal = nst.getNext()) != null) {
            System.err.println(Hex.encodeHex(nal));
            i++;
        }
        Assert.assertEquals(1019, i);
        // not much of a test but hey ... better than nothing
    }
}