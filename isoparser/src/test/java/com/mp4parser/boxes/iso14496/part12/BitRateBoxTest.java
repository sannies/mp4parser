package com.mp4parser.boxes.iso14496.part12;

import com.mp4parser.ParsableBox;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class BitRateBoxTest extends BoxRoundtripTest {

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

    public BitRateBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }
}