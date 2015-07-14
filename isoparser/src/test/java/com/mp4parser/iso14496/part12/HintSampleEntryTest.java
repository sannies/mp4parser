package com.mp4parser.iso14496.part12;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FileTypeBox;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

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


    public HintSampleEntryTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }
}