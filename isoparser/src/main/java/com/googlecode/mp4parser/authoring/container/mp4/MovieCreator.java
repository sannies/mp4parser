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
package com.googlecode.mp4parser.authoring.container.mp4;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.SchemeTypeBox;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.CencMp4TrackImplImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Shortcut to build a movie from an MP4 file.
 */
public class MovieCreator {

    public static Movie build(String file) throws IOException {
        return build(new FileDataSourceImpl(new File(file)));
    }

    /**
     * Creates <code>Movie</code> object from a <code>ReadableByteChannel</code>.
     *
     * @param channel input channel
     * @return a representation of the movie
     * @throws IOException in case of I/O error during IsoFile creation
     */
    public static Movie build(DataSource channel) throws IOException {
        IsoFile isoFile = new IsoFile(channel);
        Movie m = new Movie();
        List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
        for (TrackBox trackBox : trackBoxes) {
            SchemeTypeBox schm = Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schm[0]");
            if (schm != null && (schm.getSchemeType().equals("cenc") || schm.getSchemeType().equals("cbc1"))) {
                m.addTrack(new CencMp4TrackImplImpl(channel.toString() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]", trackBox));
            } else {
                m.addTrack(new Mp4TrackImpl(channel.toString() + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]" , trackBox));
            }
        }
        m.setMatrix(isoFile.getMovieBox().getMovieHeaderBox().getMatrix());
        return m;
    }
}
