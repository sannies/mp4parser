package com.googlecode.mp4parser.authoring;

import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.FileInputStream;
import java.io.IOException;


public class InTestMovieCreator {
    public static Movie createMovieOnlyVideo(String... names) throws IOException {
        Movie m = new Movie();
        for (String name : names) {
            Movie m1 = MovieCreator.build(new FileDataSourceImpl(InTestMovieCreator.class.getProtectionDomain().getCodeSource().getLocation().getFile() + name));
            for (Track track : m1.getTracks()) {
                if ("vide".equals(track.getHandler())) {
                    m.addTrack(track);
                }
            }

        }
        return m;
    }

}
