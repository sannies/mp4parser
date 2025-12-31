package org.mp4parser.test.boxes.iso14496.part30;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mp4parser.boxes.iso14496.part30.WebVTTSourceLabelBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@ParameterizedClass
@MethodSource("data")
public class WebVTTSourceLabelBoxTest extends BoxRoundtripTest {

    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new WebVTTSourceLabelBox(),
                        new Map.Entry[]{
                                new E("sourceLabel", "1234 \n ljhsdjkshdj \n\n")}
                });
    }

}