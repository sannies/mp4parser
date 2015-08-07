package com.googlecode.mp4parser.muxformats;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.ttml.TtmlTrackImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * Created by user on 19.08.2014.
 */
public class MuxAacH264SMPTE {
    public static void main(String[] args) throws ParserConfigurationException, SAXException, XPathExpressionException, IOException, URISyntaxException {
        Movie m = new Movie();
/*        m.addTrack(
                new SMPTETTTrackImpl(
                        new File("C:\\dev\\mp4parser-github\\ttml-example\\subtitle_5_0.xml"),
                        new File("C:\\dev\\mp4parser-github\\ttml-example\\subtitle_5_1.xml"),
                        new File("C:\\dev\\mp4parser-github\\ttml-example\\subtitle_5_2.xml")));*/
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        Document d = dbf.newDocumentBuilder().parse("C:\\dev\\mp4parser-github\\ttml-example\\subtitle_3_0.xml");
        m.addTrack(
                new TtmlTrackImpl("some subtile", Collections.singletonList(d)));
        DefaultMp4Builder builder = new DefaultMp4Builder();
        builder.build(m).writeContainer(new FileOutputStream("output.mp4").getChannel());
    }
}
