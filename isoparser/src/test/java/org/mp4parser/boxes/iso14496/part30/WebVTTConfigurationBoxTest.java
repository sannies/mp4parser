package org.mp4parser.boxes.iso14496.part30;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class WebVTTConfigurationBoxTest extends BoxRoundtripTest {


    public WebVTTConfigurationBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new WebVTTConfigurationBox(),
                        new Map.Entry[]{
                                new E("config", "1234 \n ljhsdjkshdj \n\n")}
                });
    }

}