package com.googlecode.mp4parser.boxes.dece;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.*;

import static org.junit.Assert.*;

public class ContentInformationBoxTest extends BoxRoundtripTest {

    public ContentInformationBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{new ContentInformationBox(),
                        new Map.Entry[]{
                                new E("mimeSubtypeName", "urn:dece:apid:org:castlabs:abc"),
                                new E("profileLevelIdc", "stringding"),
                                new E("codecs", "avc1.21.2121, mp4a"),
                                new E("protection", "none, cenc"),
                                new E("languages", "fr-FR, fr-CA"),
                                new E("profileLevelIdc", "urn:dece:abc"),
                                new E("brandEntries", Arrays.asList(new ContentInformationBox.BrandEntry("abcd", "561326"), new ContentInformationBox.BrandEntry("abcd", "561326"), new ContentInformationBox.BrandEntry("abcd", "561326"))),
                                new E("idEntries", Arrays.asList(new ContentInformationBox.IdEntry("urn:dece:dece:asset_id", "urn:dece:apid:org:dunno:1234")))
                        }},
                new Object[]{new ContentInformationBox(),
                        new Map.Entry[]{
                                new E("mimeSubtypeName", "urn:dece:apid:org:castlabs:abc"),
                                new E("profileLevelIdc", "stringding"),
                                new E("codecs", "avc1.21.2121, mp4a"),
                                new E("protection", "none, cenc"),
                                new E("languages", "fr-FR, fr-CA"),
                                new E("profileLevelIdc", "urn:dece:abc"),
                                new E("brandEntries", Collections.emptyList()),
                                new E("idEntries", Collections.emptyList())
                        }});
        }

    }