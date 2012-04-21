package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import junit.framework.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class SampleDependencyTypeBoxTest {

    @Test
    public void testParse() throws Exception {
        SampleDependencyTypeBox stsc = new SampleDependencyTypeBox();
        List<SampleDependencyTypeBox.Entry> l = new LinkedList<SampleDependencyTypeBox.Entry>();
        for (int i = 0; i < 0xcf; i++) {
            SampleDependencyTypeBox.Entry e = new SampleDependencyTypeBox.Entry(i);
            l.add(e);
        }
        stsc.setEntries(l);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        stsc.getBox(Channels.newChannel(baos));
        byte[] content = baos.toByteArray();

        SampleDependencyTypeBox stsc2 = new SampleDependencyTypeBox();
        stsc2.parse(new ByteBufferByteChannel(ByteBuffer.wrap(content, 8, content.length - 8)), null, content.length - 8, null);
        Assert.assertEquals(content.length, stsc2.getSize());


    }

}
