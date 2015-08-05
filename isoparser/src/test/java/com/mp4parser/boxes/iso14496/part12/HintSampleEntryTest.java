package com.mp4parser.boxes.iso14496.part12;

import com.mp4parser.ParsableBox;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Created by sannies on 14.07.2015.
 */
public class HintSampleEntryTest extends BoxRoundtripTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new HintSampleEntry("rtp "),
                        new Map.Entry[]{
                                new E("dataReferenceIndex", 0x0102),
                                new E("data", new byte[]{1,2,3,4})}
                });
    }


    public HintSampleEntryTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }
}