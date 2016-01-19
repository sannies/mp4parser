package org.mp4parser.boxes.iso14496.part12;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

public class SampleAuxiliaryInformationOffsetsBoxTest extends BoxRoundtripTest {
    public SampleAuxiliaryInformationOffsetsBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
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