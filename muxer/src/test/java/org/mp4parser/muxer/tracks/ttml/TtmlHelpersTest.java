package org.mp4parser.muxer.tracks.ttml;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static org.mp4parser.muxer.tracks.ttml.TtmlHelpers.toTime;
import static org.mp4parser.muxer.tracks.ttml.TtmlHelpers.toTimeExpression;

/**
 * Created by sannies on 06.08.2015.
 */
public class TtmlHelpersTest {
    @Test
    public void testToTime() throws Exception {
        Assert.assertEquals(-3599000, toTime("-00:59:59.000"));
        Assert.assertEquals(3599000, toTime("00:59:59.000"));
    }

    @Test
    public void testToTimeExpression() throws Exception {
        Assert.assertEquals("-00:59:59.009", toTimeExpression(-3599009));
        Assert.assertEquals("00:59:59.010", toTimeExpression(3599010));
    }

    @Test
    public void testDeepCopyDocument() throws IOException, ParserConfigurationException, SAXException, XPathExpressionException, URISyntaxException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = documentBuilderFactory.newDocumentBuilder();
        Document ttml = db.parse(new InputSource(TtmlHelpersTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/org/mp4parser/muxer/tracks/ttml/tos-chapters-en.xml"));
        //Document ttml = db.parse(new InputSource("http://localhost/mp4parser/isoparser/src/test/resources/com/googlecode/mp4parser/authoring/tracks/ttml/tos-chapters-en.xml"));
        File master = File.createTempFile("TtmlHelpersTest", "testDeepCopyDocument");
        master.delete();
        master.mkdir();

        File f = new File(master, "target");


        File targetFile = new File(f, "subs.xml");

        TtmlHelpers.deepCopyDocument(ttml, targetFile);


        Document copy = db.parse(new InputSource(targetFile.getAbsolutePath()));

        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile("//*/@backgroundImage");
        NodeList nl = (NodeList) expr.evaluate(copy, XPathConstants.NODESET);
        for (int i = 0; i < nl.getLength(); i++) {
            Node backgroundImage = nl.item(i);
            URI backgroundImageUri = URI.create(backgroundImage.getNodeValue());
            File bgImg = new File(new URI(copy.getDocumentURI()).resolve(backgroundImageUri));
            Assert.assertTrue(bgImg.exists());
            Assert.assertTrue(bgImg.delete());
            bgImg.getParentFile().delete();
        }
        Assert.assertTrue(targetFile.delete());
        Assert.assertTrue(f.delete());
        Assert.assertTrue(master.delete());


    }
}