package org.mp4parser.boxes.iso14496.part12;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class BitRateBoxTest extends BoxRoundtripTest {

    public BitRateBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new BitRateBox(),
                        new Map.Entry[]{
                                new E("bufferSizeDb", 1L),
                                new E("maxBitrate", 1L),
                                new E("avgBitrate", 21L)}
                });
    }
}