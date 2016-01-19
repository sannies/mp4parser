package org.mp4parser.boxes.iso23001.part7;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;
import org.mp4parser.ParsableBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class TrackEncryptionBoxTest extends BoxRoundtripTest {

    public TrackEncryptionBoxTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        super(parsableBoxUnderTest, properties);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new TrackEncryptionBox(),
                        new Map.Entry[]{
                                new E("default_KID", UUID.randomUUID()),
                                new E("defaultAlgorithmId", 0x1),
                                new E("defaultIvSize", 8)
                        }
                });
    }
}