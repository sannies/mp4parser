package com.googlecode.mp4parser.authoring;

import com.googlecode.mp4parser.authoring.builder.SyncSampleIntersectFinderImpl;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.IOException;
import java.nio.channels.Channels;


public class InTestMovieCreator {
    public static Movie createMovieOnlyVideo(String... names) throws IOException {
        Movie m = new Movie();
        for (String name : names) {
            Movie m1 = MovieCreator.build(Channels.newChannel(SyncSampleIntersectFinderImpl.class.getResourceAsStream(name)));
            for (Track track : m1.getTracks()) {
                if ("vide".equals(track.getHandler())) {
                    m.addTrack(track);
                }
            }

        }
        return m;
    }

}
