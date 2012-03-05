package com.googlecode.mp4parser.authoring.builder;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 1/28/12
 * Time: 2:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class CheckFragmentedToRegular {
    public static void main(String[] args) throws IOException {
        MovieCreator movieCreator = new MovieCreator();
        Movie movie = movieCreator.build(new RandomAccessFile("/home/sannies/scm/svn/mp4parser-release/output.mp4", "r").getChannel());

        DefaultMp4Builder builder1 = new DefaultMp4Builder();
        IsoFile recreated = builder1.build(movie);

        FragmentedMp4Builder builder2 = new FragmentedMp4Builder();
        builder2.setIntersectionFinder(new TwoSecondIntersectionFinder());
        IsoFile recreatedAndFragmented = builder2.build(movie);

        FileOutputStream fos1 = new FileOutputStream("/home/sannies/normalized.mp4");
        recreated.getBox(new IsoOutputStream(fos1));
        fos1.close();
        FileOutputStream fos2 = new FileOutputStream("/home/sannies/fragmented.mp4");
        recreatedAndFragmented.getBox(new IsoOutputStream(fos2));
        fos2.close();
    }
}
