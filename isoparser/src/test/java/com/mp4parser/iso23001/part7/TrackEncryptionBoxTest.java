package com.mp4parser.iso23001.part7;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.boxes.BoxRoundtripTest;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.*;

public class TrackEncryptionBoxTest extends BoxRoundtripTest {

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



    public TrackEncryptionBoxTest(Box boxUnderTest, Map.Entry<String, Object>... properties) {
        super(boxUnderTest, properties);
    }
}