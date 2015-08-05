package com.mp4parser.boxes.iso14496.part30;

import com.mp4parser.ParsableBox;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class WebVTTSourceLabelBoxTest extends BoxRoundtripTest {


    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new WebVTTSourceLabelBox(),
                        new Map.Entry[]{
                                new E("sourceLabel", "1234 \n ljhsdjkshdj \n\n")}
                });
    }

    public WebVTTSourceLabelBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

}