package com.googlecode.mp4parser;

import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.AppendTrack;
import org.mp4parser.muxer.tracks.ClippedTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Removes some samples in the middle
 */
public class RemoveSomeSamplesExample {
    public static void main(String[] args) throws IOException {
        String audioEnglish = RemoveSomeSamplesExample.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/count-english-audio.mp4";
        Movie originalMovie = MovieCreator.build(audioEnglish);

        Track audio = originalMovie.getTracks().get(0);

        Movie nuMovie = new Movie();
        nuMovie.addTrack(new AppendTrack(new ClippedTrack(audio, 0, 10), new ClippedTrack(audio, 100, audio.getSamples().size())));

        Container out = new DefaultMp4Builder().build(nuMovie);
        FileOutputStream fos = new FileOutputStream(new File("output.mp4"));
        out.writeContainer(fos.getChannel());
        fos.close();
    }

}
