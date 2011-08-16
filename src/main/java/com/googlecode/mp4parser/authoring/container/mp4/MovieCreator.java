package com.googlecode.mp4parser.authoring.container.mp4;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;

import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 8/15/11
 * Time: 8:44 AM
 * To change this template use File | Settings | File Templates.
 */
public class MovieCreator {
    public Movie build(IsoBufferWrapper isoBufferWrapper) throws IOException {
        IsoFile isoFile = new IsoFile(isoBufferWrapper);
        isoFile.parse();
        Movie m = new Movie();

        List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
        for (TrackBox trackBox : trackBoxes) {
            m.addTrack(new Mp4TrackImpl(trackBox));
        }
        return m;
    }
}
