package com.googlecode.mp4parser;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

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
