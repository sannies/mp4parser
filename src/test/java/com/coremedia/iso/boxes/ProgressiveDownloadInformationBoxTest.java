package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoOutputStream;
import junit.framework.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
        pdin.getBox(new IsoOutputStream(baos));
        byte[] fullBox = baos.toByteArray();
        IsoBufferWrapper isoBufferWrapper = new IsoBufferWrapperImpl(ByteBuffer.wrap(fullBox));
        long lengthWritten = isoBufferWrapper.readUInt32();
        Assert.assertEquals(fullBox.length, lengthWritten);
        String type = isoBufferWrapper.readString(4);
        Assert.assertEquals("pdin", type);

        ProgressiveDownloadInformationBox pdin2 = new ProgressiveDownloadInformationBox();
        pdin2.parse(isoBufferWrapper, lengthWritten - 8, null, null);
        List<ProgressiveDownloadInformationBox.Entry> parsedEntries = pdin2.getEntries();

        Assert.assertEquals(20, parsedEntries.get(0).getInitialDelay());
        Assert.assertEquals(10, parsedEntries.get(0).getRate());
        Assert.assertEquals(10, parsedEntries.get(1).getInitialDelay());
        Assert.assertEquals(20, parsedEntries.get(1).getRate());


    }
}
