package com.googlecode.mp4parser.stuff;

import com.mp4parser.IsoFile;
import com.mp4parser.authoring.Movie;
import com.mp4parser.authoring.Track;
import com.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.mp4parser.authoring.container.mp4.MovieCreator;
import com.mp4parser.authoring.tracks.Avc1ToAvc3TrackImpl;
import com.mp4parser.FileRandomAccessSourceImpl;
import com.mp4parser.Box;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Avc1ToAvc3Example {
    public static void main(String[] args) throws IOException {
        String f = Avc1ToAvc3Example.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/1365070268951.mp4";
        Movie m = MovieCreator.build(new FileInputStream(f).getChannel(), new FileRandomAccessSourceImpl(new RandomAccessFile(f, "r")), "inmem");

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
            System.err.println(box + "@-nooffsets");
        }
    }
}
