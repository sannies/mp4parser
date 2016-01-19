package com;

import org.mp4parser.Container;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.builder.Mp4Builder;
import org.mp4parser.muxer.tracks.h264.H264TrackImpl;
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
        Track audio = new H264TrackImpl(new FileDataSourceImpl("C:\\Users\\sannies\\Downloads\\test.h264"));
        v.addTrack(audio);

        Mp4Builder defaultMp4Builder = new DefaultMp4Builder();
        Container c = defaultMp4Builder.build(v);
        c.writeContainer(new FileOutputStream("output.mp4").getChannel());

    }
}
