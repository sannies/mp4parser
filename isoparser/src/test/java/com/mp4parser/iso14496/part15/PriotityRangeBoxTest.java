package com.mp4parser.iso14496.part15;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class PriotityRangeBoxTest extends BoxRoundtripTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new PriotityRangeBox(),
                        new Map.Entry[]{
                                new E("reserved1", 1),
                                new E("min_priorityId", 21),
                                new E("reserved2",  2),
                                new E("max_priorityId", 61)}
                });
    }

    public PriotityRangeBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }
}