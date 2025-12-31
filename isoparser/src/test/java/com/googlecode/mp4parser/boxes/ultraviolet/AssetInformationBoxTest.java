package com.googlecode.mp4parser.boxes.ultraviolet;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mp4parser.boxes.dece.AssetInformationBox;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@ParameterizedClass
@MethodSource("data")
public class AssetInformationBoxTest extends BoxRoundtripTest {

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
