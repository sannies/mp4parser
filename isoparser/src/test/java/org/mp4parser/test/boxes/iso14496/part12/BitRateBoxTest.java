package org.mp4parser.test.boxes.iso14496.part12;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mp4parser.boxes.iso14496.part12.BitRateBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@ParameterizedClass
@MethodSource("data")
public class BitRateBoxTest extends BoxRoundtripTest {

    public static Collection<Object[]> data() {
        return Collections.singletonList(
                new Object[]{new BitRateBox(),
                        new Map.Entry[]{
                                new E("bufferSizeDb", 1L),
                                new E("maxBitrate", 1L),
                                new E("avgBitrate", 21L)}
                });
    }
}