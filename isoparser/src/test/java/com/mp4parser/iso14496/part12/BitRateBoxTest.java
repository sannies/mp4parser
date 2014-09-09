package com.mp4parser.iso14496.part12;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

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

    public BitRateBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }
}