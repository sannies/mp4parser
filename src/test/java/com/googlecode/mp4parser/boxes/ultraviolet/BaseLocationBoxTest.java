package com.googlecode.mp4parser.boxes.ultraviolet;

import com.coremedia.iso.IsoTypeReader;
import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Map;

/**
 *
 */
public class BaseLocationBoxTest extends BoxWriteReadBase<BaseLocationBox> {

    public void testSimple() throws IOException {
        BaseLocationBox bloc = new BaseLocationBox();
        bloc.setBaseLocation("baseloc");
        bloc.setPurchaseLocation("purchloc");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bloc.getBox(Channels.newChannel(baos));
        byte[] boxAsBytes = baos.toByteArray();
        ByteBuffer bb = (ByteBuffer.wrap(boxAsBytes));
        long lengthWritten = IsoTypeReader.readUInt32(bb);
        Assert.assertEquals("bloc", IsoTypeReader.read4cc(bb));
        BaseLocationBox bloc2 = new BaseLocationBox();
        bloc2.parse(new ByteBufferByteChannel(bb), null, lengthWritten - 8, null);

        Assert.assertEquals(lengthWritten, bloc2.getSize());
        Assert.assertEquals(lengthWritten, bloc.getSize());

        Assert.assertEquals(bloc.getBaseLocation(), bloc2.getBaseLocation());
        Assert.assertEquals(bloc.getPurchaseLocation(), bloc2.getPurchaseLocation());

        Assert.assertTrue(bloc.equals(bloc2));
    }

    @Override
    public Class<BaseLocationBox> getBoxUnderTest() {
        return BaseLocationBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere) {
        addPropsHere.put("baseLocation", "this is my baselocation");
        addPropsHere.put("purchaseLocation", "this is my purchaseLocation");
    }
}
