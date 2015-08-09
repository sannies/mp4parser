package com;

import com.mp4parser.Container;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.builder.FragmentedMp4Builder;
import com.mp4parser.muxer.builder.Mp4Builder;
import com.mp4parser.muxer.container.mp4.MovieCreator;
import com.mp4parser.muxer.tracks.ttml.TtmlTrackImpl;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

/**
 * Created by sannies on 06.08.2015.
 */
public class Mux {
    public static void main(String[] args) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException, URISyntaxException {
        Movie v = new Movie(); // = MovieCreator.build("C:\\dev\\DRMTODAY-872\\31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_600000.mp4");
        Movie a1 = MovieCreator.build("C:\\dev\\DRMTODAY-872\\31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_128000_eng.mp4");
        Movie a2 = MovieCreator.build("C:\\dev\\DRMTODAY-872\\31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_128000_ita.mp4");

        /*for (Track track : a1.getTracks()) {
            v.addTrack(track);
        }

        for (Track track : a2.getTracks()) {
            v.addTrack(track);
        }*/
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document d = db.parse("C:\\dev\\dashencrypt\\a.xml");
        v.addTrack(new TtmlTrackImpl("a.xml", Collections.singletonList(d)));

        Mp4Builder defaultMp4Builder = new FragmentedMp4Builder();
        Container c = defaultMp4Builder.build(v);
        c.writeContainer(new FileOutputStream("output.mp4").getChannel());

    }
}
