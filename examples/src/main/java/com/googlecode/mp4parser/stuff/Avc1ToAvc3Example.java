package com.googlecode.mp4parser.stuff;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.Avc1ToAvc3TrackImpl;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sannies on 24.01.2015.
 */
public class Avc1ToAvc3Example {
    public static void main(String[] args) throws IOException {
        Movie m = MovieCreator.build(new FileDataSourceImpl(
                Avc1ToAvc3Example.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/1365070268951.mp4"));

        Movie m2 = new Movie();

        for (Track track : m.getTracks()) {
            if (track.getSampleDescriptionBox().getSampleEntry().getType().equals("avc1")) {
                m2.addTrack(new Avc1ToAvc3TrackImpl(track));
            } else {
                m2.addTrack(track);
            }
        }

        new FragmentedMp4Builder().build(m2).writeContainer(new FileOutputStream("output.mp4").getChannel());

        IsoFile i = new IsoFile("output.mp4");
        for (Box box : i.getBoxes()) {
            System.err.println(box + "@" + box.getOffset());
        }
    }
}
