package com.googlecode.mp4parser;

import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.builder.DefaultMp4Builder;
import com.mp4parser.muxer.container.mp4.MovieCreator;
import com.mp4parser.support.Matrix;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by sannies on 01.02.2015.
 */
public class Rotate {
    public static void main(String[] args) throws IOException {

        String f1 = AppendExample.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/1365070268951.mp4";

        Movie inMovie = MovieCreator.build(f1);
        inMovie.setMatrix(Matrix.ROTATE_90);

        new DefaultMp4Builder().build(inMovie).writeContainer(new FileOutputStream("output.mp4").getChannel());
    }
}
