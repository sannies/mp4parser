package org.mp4parser.test.boxes.iso23001.part7;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;
import org.mp4parser.boxes.iso23001.part7.ProtectionSystemSpecificHeaderBox;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;


public class ProtectionSystemSpecificHeaderBoxTest extends BoxRoundtripTest {

    public ProtectionSystemSpecificHeaderBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

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


}
