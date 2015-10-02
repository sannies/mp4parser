package org.mp4parser.boxes.iso14496.part15;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class TierBitRateBoxTest extends BoxRoundtripTest {


    public TierBitRateBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

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
}