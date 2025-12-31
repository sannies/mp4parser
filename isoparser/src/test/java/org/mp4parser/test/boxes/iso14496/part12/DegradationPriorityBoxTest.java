package org.mp4parser.test.boxes.iso14496.part12;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mp4parser.boxes.iso14496.part12.DegradationPriorityBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by sannies on 06.08.2015.
 */
@ParameterizedClass
@MethodSource("data")
public class DegradationPriorityBoxTest extends BoxRoundtripTest {

    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new DegradationPriorityBox(),
                        new Map.Entry[]{
                                new E("priorities", new int[]{1, 2, 4, 6, 8, 2, 22, 4343, 6545, 44})}
                });
    }


}