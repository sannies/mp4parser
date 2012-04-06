/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            nuMovie.addTrack(new ChangeTimeScaleTrack(track, timeScale, ChangeTimeScaleTrack.getGoodScaleFactor(track, movie, timeScale)));
        }
        return super.build(nuMovie);
    }


}
