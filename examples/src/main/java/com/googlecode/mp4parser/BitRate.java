package com.googlecode.mp4parser;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.File;
import java.io.IOException;

/**
 * Created by sannies on 02.05.2015.
 */
public class BitRate {
    public static void main(String[] args) throws IOException {
        Movie m = MovieCreator.build("c:\\content\\big_buck_bunny_1080p_h264-2min-handbraked.mp4");
        double movieDuration = 0;
        for (Track track : m.getTracks()) {
            movieDuration = Math.max((double) track.getDuration() / track.getTrackMetaData().getTimescale(), movieDuration);
        }
        // We got the full duration in seconds
        System.err.println("Bitrate in bit/s: " +
                (new File("c:\\content\\big_buck_bunny_1080p_h264-2min-handbraked.mp4").length() * 8 /movieDuration));
    }
}
