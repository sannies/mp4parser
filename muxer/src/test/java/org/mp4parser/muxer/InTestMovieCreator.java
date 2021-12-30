package org.mp4parser.muxer;

import java.io.IOException;
import java.net.URISyntaxException;

import org.mp4parser.muxer.container.mp4.MovieCreator;


public class InTestMovieCreator {
    public static Movie createMovieOnlyVideo(String... names) throws IOException, URISyntaxException {
        Movie m = new Movie();
        for (String name : names) {
            Movie m1 = MovieCreator.build((InTestMovieCreator.class.getClassLoader().getResource(name).toURI().getPath()));
            for (Track track : m1.getTracks()) {
                if ("vide".equals(track.getHandler())) {
                    m.addTrack(track);
                }
            }

        }
        return m;
    }

}
