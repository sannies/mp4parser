package org.mp4parser.boxes.iso14496.part30;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class WebVTTSampleEntryTest extends BoxRoundtripTest {


    public WebVTTSampleEntryTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

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
}