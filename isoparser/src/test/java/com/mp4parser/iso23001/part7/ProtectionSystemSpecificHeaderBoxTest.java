package com.mp4parser.iso23001.part7;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import com.mp4parser.iso23001.part7.ProtectionSystemSpecificHeaderBox;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;


public class ProtectionSystemSpecificHeaderBoxTest extends BoxRoundtripTest {

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[]{new ProtectionSystemSpecificHeaderBox(),
                        new Map.Entry[]{
                                new E("systemId", ProtectionSystemSpecificHeaderBox.OMA2_SYSTEM_ID),
                                new E("content", new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0})}},
                new Object[]{new ProtectionSystemSpecificHeaderBox(),
                        new Map.Entry[]{
                                new E("version", 1),
                                new E("keyIds", Arrays.asList(UUID.randomUUID(), UUID.randomUUID())),
                                new E("systemId", ProtectionSystemSpecificHeaderBox.OMA2_SYSTEM_ID),
                                new E("content", new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0})}});
    }

    public ProtectionSystemSpecificHeaderBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }


}
