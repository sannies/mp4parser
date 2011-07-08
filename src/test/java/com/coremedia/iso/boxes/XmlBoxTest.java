package com.coremedia.iso.boxes;

import com.coremedia.iso.*;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
        xmlBox.getBox(new IsoOutputStream(baos));

        Properties props = new Properties();
        props.put("xml ", XmlBox.class.getName() + "()");
        PropertyBoxParserImpl parser = new PropertyBoxParserImpl(props);
        IsoFile isoFile = new IsoFile(new IsoBufferWrapperImpl(ByteBuffer.wrap(baos.toByteArray())), parser);
        isoFile.parse();

        Assert.assertTrue(!isoFile.getBoxes().isEmpty());
        XmlBox xmlBox2 = (XmlBox) isoFile.getBoxes().get(0);
        Assert.assertEquals(xmlBox.getXml(), xmlBox2.getXml());
    }
}
