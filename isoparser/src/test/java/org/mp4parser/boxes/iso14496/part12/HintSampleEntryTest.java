package org.mp4parser.boxes.iso14496.part12;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by sannies on 14.07.2015.
 */
public class HintSampleEntryTest extends BoxRoundtripTest {
    public HintSampleEntryTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new HintSampleEntry("rtp "),
                        new Map.Entry[]{
                                new E("dataReferenceIndex", 0x0102),
                                new E("data", new byte[]{1, 2, 3, 4})}
                });
    }
}