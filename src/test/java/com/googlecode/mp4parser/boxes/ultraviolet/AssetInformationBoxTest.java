package com.googlecode.mp4parser.boxes.ultraviolet;

import com.coremedia.iso.IsoTypeReader;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import junit.framework.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;


public class AssetInformationBoxTest {

    @Test
    public void testInAndOut() throws IOException {
        AssetInformationBox ainf = new AssetInformationBox();
        ainf.setApid("urn:dece:apid:com:drmtoday:beta:12345abcdef");
        ainf.setProfileVersion("hdv1");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ainf.getBox(Channels.newChannel(baos));

        byte[] fullBox = baos.toByteArray();
        ByteBuffer bb = ByteBuffer.wrap(fullBox);
        long lengthWritten = IsoTypeReader.readUInt32(bb);
        Assert.assertEquals(fullBox.length, lengthWritten);
        String type = IsoTypeReader.read4cc(bb);
        Assert.assertEquals("ainf", type);

        AssetInformationBox ainf2 = new AssetInformationBox();
        ainf2.parse(new ByteBufferByteChannel(bb), null, lengthWritten - 8, null);

        Assert.assertEquals(ainf.getApid(), ainf2.getApid());
        Assert.assertEquals(ainf.getProfileVersion(), ainf2.getProfileVersion());


    }
}
