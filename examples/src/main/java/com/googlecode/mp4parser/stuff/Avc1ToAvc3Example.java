package com.googlecode.mp4parser.stuff;

import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.muxer.FileRandomAccessSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.FragmentedMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.Avc1ToAvc3TrackImpl;

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
