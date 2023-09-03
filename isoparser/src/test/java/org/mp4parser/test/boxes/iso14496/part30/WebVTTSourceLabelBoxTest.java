package org.mp4parser.test.boxes.iso14496.part30;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;
import org.mp4parser.boxes.iso14496.part30.WebVTTSourceLabelBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class WebVTTSourceLabelBoxTest extends BoxRoundtripTest {


    public WebVTTSourceLabelBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new WebVTTSourceLabelBox(),
                        new Map.Entry[]{
                                new E("sourceLabel", "1234 \n ljhsdjkshdj \n\n")}
                });
    }

}