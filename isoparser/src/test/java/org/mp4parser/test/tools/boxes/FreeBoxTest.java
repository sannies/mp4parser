package org.mp4parser.test.tools.boxes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.FreeBox;
import org.mp4parser.boxes.iso14496.part12.FreeSpaceBox;

import java.io.*;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;

public class FreeBoxTest {
    @Test
    public void testInOutNoChange() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        FreeBox fb = new FreeBox(1000);
        ByteBuffer data = fb.getData();
        ((Buffer)data).rewind();
        data.put(new byte[]{1, 2, 3, 4, 5, 6});
        fb.getBox(Channels.newChannel(baos));
        Assertions.assertEquals(1, baos.toByteArray()[8]);
        Assertions.assertEquals(2, baos.toByteArray()[9]);
        Assertions.assertEquals(3, baos.toByteArray()[10]);
        Assertions.assertEquals(4, baos.toByteArray()[11]);
    }

    @Test
    public void tesAddAndReplace() throws IOException {

        FreeBox fb = new FreeBox(1000);
        long startSize = fb.getSize();
        ByteBuffer data = fb.getData();
        ((Buffer)data).position(994);
        data.put(new byte[]{1, 2, 3, 4, 5, 6});
        FreeSpaceBox fsb = new FreeSpaceBox();
        fsb.setData(new byte[100]);
        fb.addAndReplace(fsb);
        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        f.deleteOnExit();
        FileChannel fc = new FileOutputStream(f).getChannel();
        fb.getBox(fc);
        fc.close();

        IsoFile isoFile = new IsoFile(new FileInputStream(f).getChannel());
        Assertions.assertEquals(2, isoFile.getBoxes().size());
        Assertions.assertEquals(FreeSpaceBox.TYPE, isoFile.getBoxes().get(0).getType());
        Assertions.assertEquals(FreeBox.TYPE, isoFile.getBoxes().get(1).getType());
        Assertions.assertEquals(startSize, isoFile.getBoxes().get(0).getSize() + isoFile.getBoxes().get(1).getSize());
    }
}
