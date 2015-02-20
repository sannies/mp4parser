package com.googlecode.mp4parser;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.IOException;

/**
 * Created by sannies on 18.02.2015.
 */
public class CheckGoPr1008Issue {
    public static void main(String[] args) throws IOException {
        Movie m = MovieCreator.build("C:\\Users\\sannies\\Downloads\\GOPR1008.MP4");
        for (Track track : m.getTracks()) {
            System.err.println(track);
        }
    }
}
