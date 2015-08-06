package com;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.Mp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.SMPTETTTrackImpl;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sannies on 06.08.2015.
 */
public class Mux {
    public static void main(String[] args) throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        Movie v = new Movie(); // = MovieCreator.build("C:\\dev\\DRMTODAY-872\\31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_600000.mp4");
        Movie a1 = MovieCreator.build("C:\\dev\\DRMTODAY-872\\31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_128000_eng.mp4");
        Movie a2 = MovieCreator.build("C:\\dev\\DRMTODAY-872\\31245689abb7c52a3d0721447bddd6cd_Tears_Of_Steel_128000_ita.mp4");

        /*for (Track track : a1.getTracks()) {
            v.addTrack(track);
        }

        for (Track track : a2.getTracks()) {
            v.addTrack(track);
        }*/

        v.addTrack(new SMPTETTTrackImpl(new File("C:\\dev\\dashencrypt\\a.xml")));

        Mp4Builder defaultMp4Builder = new FragmentedMp4Builder();
        Container c=  defaultMp4Builder.build(v);
        c.writeContainer(new FileOutputStream("output.mp4").getChannel());

    }
}
