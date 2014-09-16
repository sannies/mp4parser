package com.mp4parser.iso14496.part12;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class SampleAuxiliaryInformationOffsetsBoxTest extends BoxRoundtripTest {
    public SampleAuxiliaryInformationOffsetsBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Arrays.asList(
                new Object[]{new SampleAuxiliaryInformationOffsetsBox(),
                        new Map.Entry[]{
                                new E("version", 0),
                                new E("flags", 0),
                                new E("auxInfoType", null),
                                new E("auxInfoTypeParameter", null),
                                new E("offsets", new long[]{12, 34, 56, 78})
                        }
                },
                new Object[]{new SampleAuxiliaryInformationOffsetsBox(),
                        new Map.Entry[]{
                                new E("version", 0),
                                new E("flags", 1),
                                new E("auxInfoType", "abcd"),
                                new E("auxInfoTypeParameter", "defg"),
                                new E("offsets", new long[]{12, 34, 56, 78})
                        }
                },
                new Object[]{new SampleAuxiliaryInformationOffsetsBox(),
                        new Map.Entry[]{
                                new E("version", 1),
                                new E("flags", 0),
                                new E("auxInfoType", null),
                                new E("auxInfoTypeParameter", null),
                                new E("offsets", new long[]{12, 34, 56, 78})
                        }
                },
                new Object[]{new SampleAuxiliaryInformationOffsetsBox(),
                        new Map.Entry[]{
                                new E("version", 0),
                                new E("flags", 0),
                                new E("auxInfoType", null),
                                new E("auxInfoTypeParameter", null),
                                new E("offsets", new long[]{12, 34, 56, 78})
                        }
                });
    }
}