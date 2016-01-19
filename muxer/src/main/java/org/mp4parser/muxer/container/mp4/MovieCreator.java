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
package org.mp4parser.muxer.container.mp4;

import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.SchemeTypeBox;
import org.mp4parser.boxes.iso14496.part12.TrackBox;
import org.mp4parser.muxer.*;
import org.mp4parser.tools.Path;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

/**
 * Shortcut to build a movie from an MP4 file.
 */
public class MovieCreator {

    public static Movie build(String file) throws IOException {
        File f = new File(file);
        FileInputStream fis = new FileInputStream(f);
        Movie m = build(fis.getChannel(), new FileRandomAccessSourceImpl(new RandomAccessFile(f, "r")), file);
        fis.close();
        return m;
    }

    /**
     * Creates <code>Movie</code> object from a <code>ReadableByteChannel</code>.
     *
     * @param name track name to identify later
     * @param readableByteChannel the box structure is read from this channel
     * @param randomAccessSource  the samples or read from this randomAccessSource
     * @return a representation of the movie
     * @throws IOException in case of I/O error during IsoFile creation
     */
    public static Movie build(ReadableByteChannel readableByteChannel, RandomAccessSource randomAccessSource, String name) throws IOException {
        IsoFile isoFile = new IsoFile(readableByteChannel);
        Movie m = new Movie();
        List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
        for (TrackBox trackBox : trackBoxes) {
            SchemeTypeBox schm = Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schm[0]");
            if (schm != null && (schm.getSchemeType().equals("cenc") || schm.getSchemeType().equals("cbc1"))) {

                m.addTrack(new CencMp4TrackImplImpl(
                        trackBox.getTrackHeaderBox().getTrackId(), isoFile,
                        randomAccessSource, name + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]"));
            } else {
                m.addTrack(new Mp4TrackImpl(
                        trackBox.getTrackHeaderBox().getTrackId(), isoFile,
                        randomAccessSource, name + "[" + trackBox.getTrackHeaderBox().getTrackId() + "]"));
            }
        }
        m.setMatrix(isoFile.getMovieBox().getMovieHeaderBox().getMatrix());
        return m;
    }
}
