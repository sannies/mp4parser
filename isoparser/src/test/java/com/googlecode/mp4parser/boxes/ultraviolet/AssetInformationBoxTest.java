package com.googlecode.mp4parser.boxes.ultraviolet;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.googlecode.mp4parser.boxes.dece.AssetInformationBox;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.CencSampleEncryptionInformationGroupEntry;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.SampleGroupDescriptionBox;
import com.googlecode.mp4parser.util.UUIDConverter;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;


public class AssetInformationBoxTest extends BoxRoundtripTest {

    public AssetInformationBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
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
