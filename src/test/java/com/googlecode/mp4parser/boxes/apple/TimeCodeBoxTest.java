package com.googlecode.mp4parser.boxes.apple;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 3/28/12
 * Time: 10:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class TimeCodeBoxTest {
    byte[] box;

    public TimeCodeBoxTest() throws DecoderException {
        box = Hex.decodeHex((
                "00000031746d63640000000000000001" +
                "000000000000000000000bb50000007d" +
                "188c0000000f6e616d65000300003030" +
                "31").toCharArray());
    }

    @Test
    public void testParse() throws IOException {
        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(box)));
        TimeCodeBox tmcd = (TimeCodeBox) isoFile.getBoxes().get(0);
        ByteBuffer byteBuffer = ByteBuffer.allocate(l2i(tmcd.getSize()));
        tmcd.getDataReferenceIndex();
        Assert.assertTrue(tmcd.isParsed());
        tmcd.getBox(new ByteBufferByteChannel(byteBuffer));
        Assert.assertArrayEquals(box, byteBuffer.array());
    }
}
