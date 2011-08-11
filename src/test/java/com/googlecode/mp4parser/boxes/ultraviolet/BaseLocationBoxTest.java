package com.googlecode.mp4parser.boxes.ultraviolet;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoOutputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 */
public class BaseLocationBoxTest {

    @Test
    public void testSimple() throws IOException {
        BaseLocationBox bloc = new BaseLocationBox();
        bloc.setBaseLocation("baseloc");
        bloc.setPurchaseLocation("purchloc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bloc.getBox(new IsoOutputStream(baos));
        byte[] boxAsBytes = baos.toByteArray();
        IsoBufferWrapper ibw = new IsoBufferWrapperImpl(ByteBuffer.wrap(boxAsBytes));
        long lengthWritten = ibw.readUInt32();
        Assert.assertEquals("bloc", ibw.readString(4));
        BaseLocationBox bloc2 = new BaseLocationBox();
        bloc2.parse(ibw, lengthWritten - 8, null, null);

        Assert.assertEquals(lengthWritten, bloc2.getSize());
        Assert.assertEquals(lengthWritten, bloc.getSize());

        Assert.assertEquals(bloc.getBaseLocation(), bloc2.getBaseLocation());
        Assert.assertEquals(bloc.getPurchaseLocation(), bloc2.getPurchaseLocation());

        Assert.assertTrue(bloc.equals(bloc2));
    }
}
