package com;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
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
        Movie m = MovieCreator.build("C:\\Users\\sannies\\Downloads\\_convert.mp4");
        Movie audio = MovieCreator.build("C:\\content\\843D111F-E839-4597-B60C-3B8114E0AA72_AU01.mp4");
        Movie video = MovieCreator.build("C:\\content\\843D111F-E839-4597-B60C-3B8114E0AA72_ABR05.mp4");
        Movie out = new Movie();
        out.addTrack(audio.getTracks().get(0));
        out.addTrack(video.getTracks().get(0));
        Container c = new DefaultMp4Builder().build(out);
        c.writeContainer(new FileOutputStream("out.mp4").getChannel());

    }
}
