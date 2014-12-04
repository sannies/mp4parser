package com.mp4parser.iso14496.part30;

import com.coremedia.iso.boxes.Box;
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

    public WebVTTSourceLabelBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }

}