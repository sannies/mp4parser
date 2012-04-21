package com.googlecode.mp4parser.boxes;


import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

public abstract class AbstractTrackEncryptionBoxTest {

    protected AbstractTrackEncryptionBox tenc;

    @Test
    public void testRoundTrip() throws IOException {
        tenc.setDefault_KID(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6});
        tenc.setDefaultAlgorithmId(0x0a0b0c);
        tenc.setDefaultIvSize(8);


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long sizeBeforeWrite = tenc.getSize();
        tenc.getBox(Channels.newChannel(baos));
        Assert.assertEquals(baos.size(), tenc.getSize());
        Assert.assertEquals(baos.size(), sizeBeforeWrite);
        IsoFile iso = new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray())));

        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractTrackEncryptionBox);
        AbstractTrackEncryptionBox tenc2 = (AbstractTrackEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(0, tenc2.getFlags());
        Assert.assertTrue(tenc.equals(tenc2));
        Assert.assertTrue(tenc2.equals(tenc));

    }
}
