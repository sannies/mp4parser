package com.googlecode.mp4parser.boxes;


import com.mp4parser.IsoFile;
import com.mp4parser.boxes.iso23001.part7.AbstractTrackEncryptionBox;
import com.mp4parser.tools.UUIDConverter;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public abstract class AbstractTrackEncryptionBoxTest {

    protected AbstractTrackEncryptionBox tenc;

    @Test
    public void testRoundTrip() throws IOException {
        tenc.setDefault_KID(UUIDConverter.convert(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6}));
        tenc.setDefaultAlgorithmId(0x0a0b0c);
        tenc.setDefaultIvSize(8);


        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        f.deleteOnExit();
        FileChannel fc = new FileOutputStream(f).getChannel();
        tenc.getBox(fc);
        fc.close();

        IsoFile iso = new IsoFile(new FileInputStream(f).getChannel());
        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractTrackEncryptionBox);
        AbstractTrackEncryptionBox tenc2 = (AbstractTrackEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(0, tenc2.getFlags());
        Assert.assertTrue(tenc.equals(tenc2));
        Assert.assertTrue(tenc2.equals(tenc));
        iso.close();

    }
}
