package com.mp4parser.boxes.iso14496.part30;

import com.mp4parser.ParsableBox;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

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

    public WebVTTSampleEntryTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }
}