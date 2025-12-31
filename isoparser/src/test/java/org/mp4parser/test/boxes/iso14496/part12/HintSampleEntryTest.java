package org.mp4parser.test.boxes.iso14496.part12;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mp4parser.boxes.iso14496.part12.HintSampleEntry;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by sannies on 14.07.2015.
 */
@ParameterizedClass
@MethodSource("data")
public class HintSampleEntryTest extends BoxRoundtripTest {

    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new HintSampleEntry("rtp "),
                        new Map.Entry[]{
                                new E("dataReferenceIndex", 0x0102),
                                new E("data", new byte[]{1, 2, 3, 4})}
                });
    }
}