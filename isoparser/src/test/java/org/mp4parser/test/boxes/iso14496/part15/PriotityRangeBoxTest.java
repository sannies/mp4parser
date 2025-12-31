package org.mp4parser.test.boxes.iso14496.part15;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mp4parser.boxes.iso14496.part15.PriotityRangeBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@ParameterizedClass
@MethodSource("data")
public class PriotityRangeBoxTest extends BoxRoundtripTest {

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