package org.mp4parser.test.boxes.iso14496.part15;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mp4parser.boxes.iso14496.part15.TierInfoBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@ParameterizedClass
@MethodSource("data")
public class TierInfoBoxTest extends BoxRoundtripTest {

    public static Collection<Object[]> data() {
        return Collections.singletonList(
                new Object[]{new TierInfoBox(),
                        new Map.Entry[]{
                                new E("constantFrameRate", (int) 1),
                                new E("discardable", (int) 2),
                                new E("frameRate", (int) 32),
                                new E("levelIndication", (int) 2),
                                new E("profileIndication", (int) 3),
                                new E("profile_compatibility", (int) 4),
                                new E("reserved1", (int) 0),
                                new E("reserved2", (int) 0),
                                new E("tierID", (int) 21),
                                new E("visualHeight", (int) 100),
                                new E("visualWidth", (int) 200)
                        }
                });

    }
}