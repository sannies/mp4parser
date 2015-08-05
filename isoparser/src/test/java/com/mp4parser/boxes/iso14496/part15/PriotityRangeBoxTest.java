package com.mp4parser.boxes.iso14496.part15;

import com.mp4parser.ParsableBox;
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

    public PriotityRangeBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }
}