package com;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.Mp4Builder;
import com.googlecode.mp4parser.authoring.tracks.MP3TrackImpl;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by sannies on 06.08.2015.
 */
public class Mux {
    public static void main(String[] args) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException, URISyntaxException {
        Movie v = new Movie(); // = MovieCreator.build("C:\\dev\\DRMTODAY-872\\31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_600000.mp4");
        Track t = new MP3TrackImpl(new FileDataSourceImpl("C:\\Users\\sannies\\Downloads\\Allegro from Duet in C Major.mp3"));
        // Movie a2 = MovieCreator.build("C:\\dev\\DRMTODAY-872\\31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_128000_ita.mp4");

        v.addTrack(t);
        Mp4Builder defaultMp4Builder = new DefaultMp4Builder();
        Container c=  defaultMp4Builder.build(v);
        c.writeContainer(new FileOutputStream("output.mp4").getChannel());

    }
}
