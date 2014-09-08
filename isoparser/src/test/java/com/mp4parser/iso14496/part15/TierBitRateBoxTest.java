package com.mp4parser.iso14496.part15;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.CencSampleEncryptionInformationGroupEntry;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.SampleGroupDescriptionBox;
import com.googlecode.mp4parser.util.UUIDConverter;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.*;

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

    public TierBitRateBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }
}