package com.googlecode.mp4parser;

import com.mp4parser.Container;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.builder.DefaultMp4Builder;
import com.mp4parser.muxer.container.mp4.MovieCreator;
import com.mp4parser.muxer.tracks.CroppedTrack;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Shortens/Crops a track
 */
public class SingleTrackShortenExample {


    public static void main(String[] args) throws IOException {
        //Movie movie = new MovieCreator().build(new RandomAccessFile("/home/sannies/suckerpunch-distantplanet_h1080p/suckerpunch-distantplanet_h1080p.mov", "r").getChannel());
        Movie movie = MovieCreator.build("C:\\content\\843D111F-E839-4597-B60C-3B8114E0AA72_ABR05.mp4");

        List<Track> tracks = movie.getTracks();
        assert tracks.size() == 1;
        Track track = movie.getTracks().get(0);

        movie.setTracks(new LinkedList<Track>());
        // remove all tracks we will create new tracks from the old

        double startTime = 10;
        double endTime = 20;

        long startSample = findNextSyncSample(track, startTime);
        long endSample = findNextSyncSample(track, endTime);

        movie.addTrack(new CroppedTrack(track, startSample, endSample));

        Container out = new DefaultMp4Builder().build(movie);
        FileOutputStream fos = new FileOutputStream(String.format("output-%f-%f.mp4", startTime, endTime));
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);
        fc.close();
        fos.close();
    }


    private static long findNextSyncSample(Track track, double cutHere) {
        long currentSample = 0;
        double currentTime = 0;
        long[] durations = track.getSampleDurations();
        long[] syncSamples = track.getSyncSamples();
        for (int i = 0; i < durations.length; i++) {
            long delta = durations[i];

            if ((syncSamples == null || syncSamples.length > 0 || Arrays.binarySearch(syncSamples, currentSample + 1) >= 0)
                    && currentTime > cutHere) {
                return i;
            }
            currentTime += (double) delta / (double) track.getTrackMetaData().getTimescale();
            currentSample++;
        }
        return currentSample;
    }


}
