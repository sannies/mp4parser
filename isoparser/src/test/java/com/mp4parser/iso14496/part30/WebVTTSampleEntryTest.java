package com.mp4parser.iso14496.part30;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class WebVTTSampleEntryTest extends BoxRoundtripTest {


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        WebVTTSampleEntry wvtt = new WebVTTSampleEntry();
        WebVTTConfigurationBox vttC = new WebVTTConfigurationBox();
        vttC.setConfig("abc");
        WebVTTSourceLabelBox vlab = new WebVTTSourceLabelBox();
        vlab.setSourceLabel("dunno");
        wvtt.addBox(vttC);
        wvtt.addBox(vlab);

        return Collections.singletonList(
                new Object[]{wvtt,
                        new Map.Entry[]{}
                });
    }

    public WebVTTSampleEntryTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }
}