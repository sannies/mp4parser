package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.PropertyBoxParserImpl;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.Properties;

/**
 *
 */
public class XmlBoxTest {

    @Test
    public void simpleRoundTrip() throws IOException {
        XmlBox xmlBox = new XmlBox();
        xmlBox.setXml("<a></a>"); // but the box doesnt care if well-formed
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        xmlBox.getBox(Channels.newChannel(baos));

        Properties props = new Properties();
        props.put("xml ", XmlBox.class.getName());
        PropertyBoxParserImpl parser = new PropertyBoxParserImpl(props);
        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel((ByteBuffer) ByteBuffer.wrap(baos.toByteArray()).rewind()), parser);

        Assert.assertTrue(!isoFile.getBoxes().isEmpty());
        XmlBox xmlBox2 = (XmlBox) isoFile.getBoxes().get(0);
        Assert.assertEquals(xmlBox.getXml(), xmlBox2.getXml());
    }
}
