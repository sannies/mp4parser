package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import junit.framework.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class ProgressiveDownloadInformationBoxTest {
    @Test
    public void testInAndOut() throws IOException {
        ProgressiveDownloadInformationBox pdin = new ProgressiveDownloadInformationBox();
        List<ProgressiveDownloadInformationBox.Entry> entries = new LinkedList<ProgressiveDownloadInformationBox.Entry>();
        entries.add(new ProgressiveDownloadInformationBox.Entry(10, 20));
        entries.add(new ProgressiveDownloadInformationBox.Entry(20, 10));
        pdin.setEntries(entries);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        pdin.getBox(Channels.newChannel(baos));
        byte[] fullBox = baos.toByteArray();
        ByteBuffer bb = ByteBuffer.wrap(fullBox);
        long lengthWritten = IsoTypeReader.readUInt32(bb);
        Assert.assertEquals(fullBox.length, lengthWritten);
        String type = IsoTypeReader.read4cc(bb);
        Assert.assertEquals("pdin", type);

        ProgressiveDownloadInformationBox pdin2 = new ProgressiveDownloadInformationBox();
        pdin2.parse(new ByteBufferByteChannel(bb), null, lengthWritten - 8, null);
        List<ProgressiveDownloadInformationBox.Entry> parsedEntries = pdin2.getEntries();

        Assert.assertEquals(20, parsedEntries.get(0).getInitialDelay());
        Assert.assertEquals(10, parsedEntries.get(0).getRate());
        Assert.assertEquals(10, parsedEntries.get(1).getInitialDelay());
        Assert.assertEquals(20, parsedEntries.get(1).getRate());


    }
}
