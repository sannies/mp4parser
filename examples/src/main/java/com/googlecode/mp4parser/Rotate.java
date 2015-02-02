package com.googlecode.mp4parser;

import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.util.Matrix;

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
