package com;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.Mp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.googlecode.mp4parser.util.Mp4Arrays;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by sannies on 19.11.2015.
 */
public class ChopSuey {
    public static void main(String[] args) throws IOException {
        Movie m = MovieCreator.build("C:\\dev\\mp4parser\\schaf-schleppt-knüppel.mp4");
        Track videoTrack = null;
        for (Track track : m.getTracks()) {
            if ("vide".equals(track.getHandler())) {
                videoTrack = track;
            }
        }
        if (videoTrack == null) {
            throw new RuntimeException("You need a video track!");
        }
        int refNumSamples = videoTrack.getSamples().size();
        long[] refSampleDuration = videoTrack.getSampleDurations();
        long[] syncSamples = videoTrack.getSyncSamples();
        double[] syncSampleTimes = new double[0];
        int refIndex = 0;
        double refTime = 0;
        for (long syncSample : syncSamples) {
            while (refIndex < syncSample - 1 && refIndex < refNumSamples) {
                refTime += (double) refSampleDuration[refIndex] / videoTrack.getTrackMetaData().getTimescale();
                refIndex++;
            }
            syncSampleTimes = Mp4Arrays.copyOfAndAppend(syncSampleTimes, refTime);

        }
        Map<Track, List<Track>> tracks = new HashMap<Track, List<Track>>();
        for (Track track : m.getTracks()) {
            List<Track> chops = new ArrayList<Track>();

            int lastStart = 0;
            int index = 0;
            int numSamples = track.getSamples().size();
            long[] durations = track.getSampleDurations();
            double time = 0;
            int timeIndex = 0;

            while (index < numSamples) {
                if (timeIndex >= syncSampleTimes.length) {
                    chops.add(new CroppedTrack(track, lastStart, numSamples));
                    System.err.println("Added partial track for " + track.getTrackMetaData().getTrackId() + " from sample " + lastStart + " to " + numSamples);
                    break;
                }
                if (time >= syncSampleTimes[timeIndex]) {
                    if (lastStart != index) {
                        chops.add(new CroppedTrack(track, lastStart, index));
                        System.err.println("Added partial track for " + track.getTrackMetaData().getTrackId() + " from sample " + lastStart + " to " + index);
                        lastStart = index;
                    }
                    timeIndex++;
                }
                time += (double) durations[index] / track.getTrackMetaData().getTimescale();
                index++;

            }
            if (chops.size() > 0) {
                tracks.put(track, chops);
            }


        }
        Mp4Builder b = new DefaultMp4Builder();

        for (int i = 0; i < syncSamples.length; i++) {
            Movie chopped = new Movie();

            for (Track track : tracks.keySet()) {
                chopped.addTrack(tracks.get(track).get(i));
            }

            b.build(chopped).writeContainer(new FileOutputStream("out-" + i + ".mp4").getChannel());
        }

    }
}
