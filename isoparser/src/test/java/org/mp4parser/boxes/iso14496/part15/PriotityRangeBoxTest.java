package org.mp4parser.boxes.iso14496.part15;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class PriotityRangeBoxTest extends BoxRoundtripTest {

    public PriotityRangeBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new PriotityRangeBox(),
                        new Map.Entry[]{
                                new E("reserved1", 1),
                                new E("min_priorityId", 21),
                                new E("reserved2", 2),
                                new E("max_priorityId", 61)}
                });
    }
}