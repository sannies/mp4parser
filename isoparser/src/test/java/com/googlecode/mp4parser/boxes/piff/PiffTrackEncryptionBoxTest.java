package com.googlecode.mp4parser.boxes.piff;

import com.googlecode.mp4parser.boxes.AbstractTrackEncryptionBoxTest;
import org.junit.jupiter.api.BeforeEach;
import org.mp4parser.boxes.microsoft.PiffTrackEncryptionBox;


public class PiffTrackEncryptionBoxTest extends AbstractTrackEncryptionBoxTest {


    @BeforeEach
    public void setUp() throws Exception {
        tenc = new PiffTrackEncryptionBox();
    }

}
