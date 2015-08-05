package com.mp4parser.boxes.samplegrouping;

import com.mp4parser.ParsableBox;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class CencSampleEncryptionInformationGroupEntryTest extends BoxRoundtripTest {


    public CencSampleEncryptionInformationGroupEntryTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        CencSampleEncryptionInformationGroupEntry seig1 = new CencSampleEncryptionInformationGroupEntry();
        seig1.setEncrypted(true);
        seig1.setKid(UUID.randomUUID());
        seig1.setIvSize(8);
        CencSampleEncryptionInformationGroupEntry seig2 = new CencSampleEncryptionInformationGroupEntry();
        seig2.setEncrypted(false);
        seig2.setKid(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        return Arrays.asList(
                new Object[]{new SampleGroupDescriptionBox(),
                        new Map.Entry[]{
                                new E("groupEntries", Arrays.asList(seig1))}},
                new Object[]{new SampleGroupDescriptionBox(),
                        new Map.Entry[]{
                                new E("groupEntries", Arrays.asList(seig1, seig2))
                        }},
                new Object[]{new SampleGroupDescriptionBox(),
                        new Map.Entry[]{
                                new E("groupEntries", Arrays.asList(seig2))}});
    }



}