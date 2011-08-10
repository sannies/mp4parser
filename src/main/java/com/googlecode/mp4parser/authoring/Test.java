package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.BufferedOutputStream;
import java.io.File;
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
        IsoFile isoFile = new IsoFile(new IsoBufferWrapperImpl(new File("/home/sannies/suckerpunch-samurai_h640w.mov")));
        isoFile.parse();
        Movie movie = new Movie(isoFile);

        IsoFile out = new DefaultMp4IsoBuilder().build(movie);

        FileOutputStream fos = new FileOutputStream("/home/sannies/suckerpunch-samurai_h640w.mp4");
        BufferedOutputStream bos = new BufferedOutputStream(fos);
        out.getBox(new IsoOutputStream(bos));
        bos.close();
        IsoFile reread = new IsoFile(new IsoBufferWrapperImpl(new File("/home/sannies/suckerpunch-samurai_h640w.mp4")));
        reread.parse();

    }
}
