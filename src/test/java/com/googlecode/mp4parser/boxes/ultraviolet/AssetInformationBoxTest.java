package com.googlecode.mp4parser.boxes.ultraviolet;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoOutputStream;
import junit.framework.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 7/1/11
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class AssetInformationBoxTest {

    @Test
    public void testInAndOut() throws IOException {
        AssetInformationBox ainf = new AssetInformationBox();
        ainf.setApid("urn:dece:apid:com:drmtoday:beta:12345abcdef");
        ainf.setProfileVersion("hdv1");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ainf.getBox(new IsoOutputStream(baos));

        byte[] fullBox = baos.toByteArray();
        IsoBufferWrapper isoBufferWrapper = new IsoBufferWrapperImpl(ByteBuffer.wrap(fullBox));
        long lengthWritten = isoBufferWrapper.readUInt32();
        Assert.assertEquals(fullBox.length, lengthWritten);
        String type = isoBufferWrapper.readString(4);
        Assert.assertEquals("ainf", type);

        AssetInformationBox ainf2 = new AssetInformationBox();
        ainf2.parse(isoBufferWrapper, lengthWritten - 8, null, null);

        Assert.assertEquals(ainf.getApid(), ainf2.getApid());
        Assert.assertEquals(ainf.getProfileVersion(), ainf2.getProfileVersion());


    }
}
