package org.mp4parser.streaming.containersource;

import org.junit.Assert;
import org.junit.Test;
import org.mp4parser.streaming.input.mp4.DiscardingByteArrayOutputStream;

/**
 * Created by sannies on 05.08.2015.
 */
public class DiscardingByteArrayOutputStreamTest {

    @Test
    public void testSimple() throws Exception {
        DiscardingByteArrayOutputStream dbaos = new DiscardingByteArrayOutputStream();
        dbaos.write(0);
        dbaos.write(1);
        dbaos.write(2);
        dbaos.write(3);
        dbaos.write(4);
        dbaos.write(5);
        dbaos.write(6);
        dbaos.write(7);
        byte[] b = dbaos.get(3, 3);
        Assert.assertArrayEquals(new byte[]{3, 4, 5}, b);
        dbaos.discardTo(3);
        b = dbaos.get(3, 3);
        Assert.assertArrayEquals(new byte[]{3, 4, 5}, b);
        dbaos.discardTo(3);
        b = dbaos.get(3, 3);
        Assert.assertArrayEquals(new byte[]{3, 4, 5}, b);

        dbaos.discardTo(4);
        b = dbaos.get(4, 3);
        Assert.assertArrayEquals(new byte[]{4, 5, 6}, b);

    }
}