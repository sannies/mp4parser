package com.googlecode.mp4parser.boxes.piff;

import com.googlecode.mp4parser.boxes.AbstractTrackEncryptionBoxTest;
import com.mp4parser.boxes.microsoft.PiffTrackEncryptionBox;
import org.junit.Before;


public class PiffTrackEncryptionBoxTest extends AbstractTrackEncryptionBoxTest {


    @Before
    public void setUp() throws Exception {
        tenc = new PiffTrackEncryptionBox();
    }

}
