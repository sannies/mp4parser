package com.googlecode.mp4parser.boxes.basemediaformat;

import com.googlecode.mp4parser.boxes.AbstractTrackEncryptionBoxTest;
import com.mp4parser.iso23001.part7.TrackEncryptionBox;
import org.junit.Before;


public class TrackEncryptionBoxTest extends AbstractTrackEncryptionBoxTest {


    @Before
    public void setUp() throws Exception {
        tenc = new TrackEncryptionBox();
    }

}
