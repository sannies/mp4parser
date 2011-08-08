package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.coremedia.iso.boxes.TrackBox;

import java.util.LinkedList;
import java.util.List;

/**
 *
 */
public class Movie {
    List<Track> tracks = new LinkedList<Track>();
    MovieMetaData movieMetaData = new MovieMetaData();

    public Movie(IsoFile isoFile) {
        assert isoFile.isParsed();
        MovieHeaderBox mvhd = isoFile.getMovieBox().getMovieHeaderBox();
        movieMetaData.setDuration(mvhd.getDuration());
        movieMetaData.setTimescale(mvhd.getTimescale());

        List<TrackBox> trackBoxes = isoFile.getMovieBox().getBoxes(TrackBox.class);
        for (TrackBox trackBox : trackBoxes) {
            tracks.add(new Track(trackBox));
        }
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void addTrack(Track track) {
        // do some checking
        // perhaps the movie needs to get longer!
        tracks.add(track);
    }

    public MovieMetaData getMovieMetaData() {
        return movieMetaData;
    }

    public void setMovieMetaData(MovieMetaData movieMetaData) {
        this.movieMetaData = movieMetaData;
    }

    @Override
    public String toString() {
        String s = "Movie{ ";
        for (Track track : tracks) {
            s += "track_" + track.getTrackMetaData().getTrackId() + " (" + track.getType() + "), ";
        }

        s += ", movieMetaData=" + movieMetaData + '}';
        return s;
    }


}
