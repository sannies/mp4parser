package org.mp4parser.test.boxes.iso23001.part7;

import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mp4parser.boxes.iso23001.part7.TrackEncryptionBox;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@ParameterizedClass
@MethodSource("data")
public class TrackEncryptionBoxTest extends BoxRoundtripTest {

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