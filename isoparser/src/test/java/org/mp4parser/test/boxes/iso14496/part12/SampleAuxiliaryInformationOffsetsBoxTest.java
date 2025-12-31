package org.mp4parser.test.boxes.iso14496.part12;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mp4parser.boxes.iso14496.part12.SampleAuxiliaryInformationOffsetsBox;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@ParameterizedClass
@MethodSource("data")
public class SampleAuxiliaryInformationOffsetsBoxTest extends BoxRoundtripTest {

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