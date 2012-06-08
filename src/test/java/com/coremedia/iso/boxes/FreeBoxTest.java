package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import junit.framework.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;

public class FreeBoxTest {
    @Test
    public void testInOutNoChange() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FreeBox fb = new FreeBox(1000);
        ByteBuffer data = fb.getData();
        data.rewind();
        data.put(new byte[]{1,2,3,4,5,6});
        fb.getBox(Channels.newChannel(baos));
        Assert.assertEquals(baos.toByteArray()[8], 1);
        Assert.assertEquals(baos.toByteArray()[9], 2);
        Assert.assertEquals(baos.toByteArray()[10], 3);
        Assert.assertEquals(baos.toByteArray()[11], 4);
    }

    @Test
    public void tesAddAndReplace() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        FreeBox fb = new FreeBox(1000);
        long startSize = fb.getSize();
        ByteBuffer data = fb.getData();
        data.position(994);
        data.put(new byte[]{1,2,3,4,5,6});
        FreeSpaceBox fsb = new FreeSpaceBox();
        fsb.setData(new byte[100]);
        fb.addAndReplace(fsb);
        fb.getBox(Channels.newChannel(baos));
        int l = baos.toByteArray().length - 1;
        Assert.assertEquals(baos.toByteArray()[l], 6);
        Assert.assertEquals(baos.toByteArray()[l - 1], 5);
        Assert.assertEquals(baos.toByteArray()[l - 2], 4);
        Assert.assertEquals(baos.toByteArray()[l - 3], 3);

        IsoFile isoFile = new IsoFile(Channels.newChannel(new ByteArrayInputStream(baos.toByteArray())));
        Assert.assertEquals(2, isoFile.getBoxes().size());
        Assert.assertEquals(FreeSpaceBox.TYPE, isoFile.getBoxes().get(0).getType());
        Assert.assertEquals(FreeBox.TYPE, isoFile.getBoxes().get(1).getType());
        Assert.assertEquals(startSize, isoFile.getBoxes().get(0).getSize() + isoFile.getBoxes().get(1).getSize());
    }
}
