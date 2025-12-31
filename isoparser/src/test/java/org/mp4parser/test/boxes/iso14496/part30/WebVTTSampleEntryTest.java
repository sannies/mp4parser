package org.mp4parser.test.boxes.iso14496.part30;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mp4parser.boxes.iso14496.part30.WebVTTConfigurationBox;
import org.mp4parser.boxes.iso14496.part30.WebVTTSampleEntry;
import org.mp4parser.boxes.iso14496.part30.WebVTTSourceLabelBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@ParameterizedClass
@MethodSource("data")
public class WebVTTSampleEntryTest extends BoxRoundtripTest {

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