package com.googlecode.mp4parser;

import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.IOException;

/**
 * Created by sannies on 24.07.2015.
 */
public class SimpleParse {
    public static void main(String[] args) throws IOException {
        Movie m = MovieCreator.build("C:\\Users\\sannies\\Downloads\\3ae39746-7e83-4653-860b-78a59e6ef474 (3).mp4");
        for (Track track : m.getTracks()) {
            System.err.print(track.getHandler());
            System.err.print(track.getSamples().size());
        }
    }
}
