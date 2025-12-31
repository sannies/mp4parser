package com.googlecode.mp4parser.boxes.basemediaformat;

import com.googlecode.mp4parser.boxes.AbstractTrackEncryptionBoxTest;
import org.junit.jupiter.api.BeforeEach;
import org.mp4parser.boxes.iso23001.part7.TrackEncryptionBox;


public class TrackEncryptionBoxTest extends AbstractTrackEncryptionBoxTest {


    @BeforeEach
    public void setUp() throws Exception {
        tenc = new TrackEncryptionBox();
    }

}
