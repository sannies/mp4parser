package com.mp4parser.iso14496.part30;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import com.mp4parser.iso14496.part15.PriotityRangeBox;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class WebVTTConfigurationBoxTest  extends BoxRoundtripTest {


    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new WebVTTConfigurationBox(),
                        new Map.Entry[]{
                                new E("config", "1234 \n ljhsdjkshdj \n\n")}
                });
    }

    public WebVTTConfigurationBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }

}