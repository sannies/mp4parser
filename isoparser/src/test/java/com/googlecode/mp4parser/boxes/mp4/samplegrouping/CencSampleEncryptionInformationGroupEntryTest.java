package com.googlecode.mp4parser.boxes.mp4.samplegrouping;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import com.googlecode.mp4parser.util.UUIDConverter;
import org.junit.runners.Parameterized;

import java.util.*;

public class CencSampleEncryptionInformationGroupEntryTest extends BoxRoundtripTest {


    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        CencSampleEncryptionInformationGroupEntry seig1 = new CencSampleEncryptionInformationGroupEntry();
        seig1.setEncrypted(true);
        seig1.setKid(UUID.randomUUID());
        seig1.setIvSize(8);
        CencSampleEncryptionInformationGroupEntry seig2 = new CencSampleEncryptionInformationGroupEntry();
        seig2.setEncrypted(false);
        seig2.setKid(UUIDConverter.convert(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}));

        return Arrays.asList(
                new Object[]{new SampleGroupDescriptionBox(),
                        new Map.Entry[]{
                                new E("groupingType", "seig"),
                                new E("groupEntries", Arrays.asList(seig1))}},
                new Object[]{new SampleGroupDescriptionBox(),
                        new Map.Entry[]{
                                new E("groupingType", "seig"),
                                new E("groupEntries", Arrays.asList(seig2))}});
    }

    public CencSampleEncryptionInformationGroupEntryTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }



}