package com.googlecode.mp4parser.authoring.builder.smoothstreaming;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.ChangeTimeScaleTrack;

import java.io.IOException;

/**
 * Creates a fragmented IsoFile data structure according to Microsofts ISMV
 * Smooth Streaming format.
 */
public class IsmvBuilder extends FragmentedMp4Builder {
    long timeScale = 10000000;

    public IsmvBuilder() {

    }

    @Override
    public IsoFile build(Movie movie) throws IOException {
        Movie nuMovie = new Movie();
        movie.setMovieMetaData(movie.getMovieMetaData());
        for (Track track : movie.getTracks()) {
            nuMovie.addTrack(new ChangeTimeScaleTrack(track, timeScale));
        }
        return super.build(nuMovie);
    }


}
