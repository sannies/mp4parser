package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 8/7/11
 * Time: 10:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        Movie movie = new MovieCreator().build(new FileInputStream("/home/sannies/suckerpunch-samurai_h640w.mov").getChannel());

        IsoFile out = new DefaultMp4Builder().build(movie);

        FileOutputStream fos = new FileOutputStream("/home/sannies/suckerpunch-samurai_h640w.mp4");

        out.getBox(fos.getChannel());
        fos.close();

        IsoFile reread = new IsoFile(new FileOutputStream("/home/sannies/suckerpunch-samurai_h640w.mp4").getChannel());

    }
}
