package com.googlecode.mp4parser.boxes.ultraviolet;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;
import org.mp4parser.boxes.dece.AssetInformationBox;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;


public class AssetInformationBoxTest extends BoxRoundtripTest {

    public AssetInformationBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{new AssetInformationBox(),
                        new Map.Entry[]{
                                new E("apid", "urn:dece:apid:org:castlabs:abc"),
                                new E("hidden", false),
                                new E("profileVersion", "1001")}},
                new Object[]{new AssetInformationBox(),
                        new Map.Entry[]{
                                new E("apid", "urn:dece:apid:org:castlabs:abc2"),
                                new E("hidden", true),
                                new E("profileVersion", "0001")}});
    }




}
