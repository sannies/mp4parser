package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.mp4parser.iso14496.part12.BitRateBox;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.*;

public class FileTypeBoxTest extends BoxRoundtripTest {
    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new FileTypeBox(),
                        new Map.Entry[]{
                                new E("majorBrand", "mp45"),
                                new E("minorVersion", 0x124334L),
                                new E("compatibleBrands", Arrays.asList("abcd", "hjkl"))}
                });
    }


    public FileTypeBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }
}