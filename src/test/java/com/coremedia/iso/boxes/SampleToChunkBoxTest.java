package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 24.02.11
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class SampleToChunkBoxTest extends TestCase {


    public void testParse() throws Exception {
        SampleToChunkBox stsc = new SampleToChunkBox();
        List<SampleToChunkBox.Entry> l = new LinkedList<SampleToChunkBox.Entry>();
        for (int i = 0; i < 5; i++) {
            SampleToChunkBox.Entry e = new SampleToChunkBox.Entry(i, 1, i * i);
            l.add(e);
        }
        stsc.setEntries(l);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stsc.getBox(Channels.newChannel(baos));
        byte[] content = baos.toByteArray();

        SampleToChunkBox stsc2 = new SampleToChunkBox();
        stsc2.parse(new ByteBufferByteChannel(ByteBuffer.wrap(content, 8, content.length - 8)), null, content.length - 8, null);
        Assert.assertEquals(content.length, stsc2.getSize());


    }
}
