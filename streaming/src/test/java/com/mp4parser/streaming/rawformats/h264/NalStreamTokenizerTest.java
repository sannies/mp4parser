package com.mp4parser.streaming.rawformats.h264;

import com.mp4parser.tools.Hex;
import org.junit.Test;

/**
 * Created by sannies on 15.08.2015.
 */
public class NalStreamTokenizerTest {
    @Test
    public void testTokenize() throws Exception {
        NalStreamTokenizer nst = new NalStreamTokenizer(
                NalStreamTokenizerTest.class.getResourceAsStream("/com/mp4parser/streaming/rawformats/h264/tos.h264"),
                new byte[]{0, 0, 1},
                new byte[]{0, 0, 0}

        );

        byte[] nal;


        while ((nal = nst.getNext()) != null) {
            System.err.println(Hex.encodeHex(nal));
        }
    }
}