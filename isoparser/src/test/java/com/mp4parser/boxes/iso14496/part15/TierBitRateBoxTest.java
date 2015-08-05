package com.mp4parser.boxes.iso14496.part15;

import com.mp4parser.ParsableBox;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.*;

public class TierBitRateBoxTest extends BoxRoundtripTest {


    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new TierBitRateBox(),
                        new Map.Entry[]{
                                new E("avgBitRate", 32l),
                                new E("baseBitRate", (long) 21),
                                new E("maxBitRate", (long) 32),
                                new E("tierAvgBitRate", (long) 45),
                                new E("tierBaseBitRate", (long) 65),
                                new E("tierMaxBitRate", (long) 67)}
                });
    }

    public TierBitRateBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }
}