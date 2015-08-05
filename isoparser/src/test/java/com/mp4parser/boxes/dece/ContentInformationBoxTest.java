package com.mp4parser.boxes.dece;

import com.mp4parser.ParsableBox;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.*;

public class ContentInformationBoxTest extends BoxRoundtripTest {

    public ContentInformationBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        Map<String, String> aBrandEntries = new LinkedHashMap<String, String>();
        aBrandEntries.put("abcd", "561326");
        aBrandEntries.put("abcd", "561326");
        aBrandEntries.put("abcd", "561326");

        Map<String, String> aIdEntries = new LinkedHashMap<String, String>();
        aIdEntries.put("urn:dece:dece:asset_id", "urn:dece:apid:org:dunno:1234");



        return Arrays.asList(
                new Object[]{new ContentInformationBox(),
                        new Map.Entry[]{
                                new E("mimeSubtypeName", "urn:dece:apid:org:castlabs:abc"),
                                new E("profileLevelIdc", "stringding"),
                                new E("codecs", "avc1.21.2121, mp4a"),
                                new E("protection", "none, cenc"),
                                new E("languages", "fr-FR, fr-CA"),
                                new E("profileLevelIdc", "urn:dece:abc"),
                                new E("brandEntries", aBrandEntries),
                                new E("idEntries", aIdEntries)
                        }},
                new Object[]{new ContentInformationBox(),
                        new Map.Entry[]{
                                new E("mimeSubtypeName", "urn:dece:apid:org:castlabs:abc"),
                                new E("profileLevelIdc", "stringding"),
                                new E("codecs", "avc1.21.2121, mp4a"),
                                new E("protection", "none, cenc"),
                                new E("languages", "fr-FR, fr-CA"),
                                new E("profileLevelIdc", "urn:dece:abc"),
                                new E("brandEntries", Collections.emptyMap()),
                                new E("idEntries", Collections.emptyMap())
                        }});
    }

}