package com.googlecode.mp4parser.boxes.cenc;

import com.coremedia.iso.IsoFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 6/8/12
 * Time: 3:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ProtectionSystemSpecificHeaderBoxTest {
    @Test
    public void testRoundtrip() throws IOException {
        ProtectionSystemSpecificHeaderBox pssh = new ProtectionSystemSpecificHeaderBox();
        pssh.setSystemId(ProtectionSystemSpecificHeaderBox.OMA2_SYSTEM_ID);
        byte[] content = new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 0};
        pssh.setContent(content);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pssh.getBox(Channels.newChannel(baos));
        IsoFile isoFile = new IsoFile(Channels.newChannel(new ByteArrayInputStream(baos.toByteArray())));
        Assert.assertEquals(1, isoFile.getBoxes().size());
        Assert.assertTrue(isoFile.getBoxes().get(0) instanceof ProtectionSystemSpecificHeaderBox);

        Assert.assertArrayEquals(((ProtectionSystemSpecificHeaderBox) isoFile.getBoxes().get(0)).getContent(), content);

    }
}
