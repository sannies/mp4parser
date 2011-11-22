package com.googlecode.mp4parser.boxes.basemediaformat;

import com.googlecode.mp4parser.boxes.AbstractSampleEncryptionBoxTest;
import org.junit.Before;


public class SampleEncryptionBoxTest extends AbstractSampleEncryptionBoxTest {


    @Before
    public void setUp() throws Exception {
        senc = new SampleEncryptionBox();
    }

}
