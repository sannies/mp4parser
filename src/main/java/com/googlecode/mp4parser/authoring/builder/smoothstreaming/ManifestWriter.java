package com.googlecode.mp4parser.authoring.builder.smoothstreaming;


import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;

import java.io.IOException;

public interface ManifestWriter {
    String getManifest(Movie inputs) throws IOException;

    long getBitrate(Track track);

    long[] calculateFragmentDurations(Track track, Movie movie);

}
