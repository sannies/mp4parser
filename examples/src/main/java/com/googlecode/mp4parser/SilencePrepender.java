package com.googlecode.mp4parser;


import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
import com.googlecode.mp4parser.authoring.tracks.SilenceTrackImpl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class SilencePrepender {
    public static void main(String[] args) throws IOException {

        Movie audioMovie = MovieCreator.build("/home/sannies/scm/svn/mp4parser/silence/sample.mp4");


        Movie result = new Movie();
        Track audio = audioMovie.getTracks().get(0);

        Track silence = new SilenceTrackImpl(audio, 2000);

        result.addTrack(new AppendTrack(silence, audio));

        Container isoFile = new DefaultMp4Builder().build(result);

        FileChannel fc = new RandomAccessFile(String.format("output.mp4"), "rw").getChannel();
        fc.position(0);
        isoFile.writeContainer(fc);
        fc.close();

    }
}
