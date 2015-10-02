package com.googlecode.mp4parser.stuff;

import org.mp4parser.Container;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

/**
 *
 */
public class ReadWriteExample {


    public static void main(String[] args) throws IOException {


        //Movie video = mc.build(Channels.newChannel(ReadWriteExample.class.getResourceAsStream("/smoothstreaming/video-128h-75kbps.mp4")));
        Movie video = MovieCreator.build("/home/sannies/scm/svn/mp4parser/Solekai022_854_29_640x75_MaxSdSubtitle.uvu");

        //IsoFile out1 = new FragmentedMp4Builder().build(video);
        Container out2 = new DefaultMp4Builder().build(video);


       /* long starttime1 = System.currentTimeMillis();
        FileChannel fc1 = new RandomAccessFile("video-128h-75kbps.fmp4", "rw").getChannel();
        fc1.position(0);
        out1.getBox(fc1);
        long size1 = fc1.size();
        fc1.truncate(fc1.position());
        fc1.close();
        System.err.println("Writing " + size1 / 1024 / 1024 + "MB took " + (System.currentTimeMillis() - starttime1));*/

        long starttime2 = System.currentTimeMillis();
        FileChannel fc2 = new RandomAccessFile("output_uvu.mp4", "rw").getChannel();
        out2.writeContainer(fc2);
        long size2 = fc2.size();
        fc2.truncate(fc2.position());
        fc2.close();
        System.err.println("Writing " + size2 / 1024 / 1024 + "MB took " + (System.currentTimeMillis() - starttime2));

    }

    /*
      public static void main(String[] args) throws IOException {
        MovieCreator mc = new MovieCreator();

        Movie video = mc.build(new FileInputStream("/media/scratch/qualitaetstest_cinovu_sherminfiles/abendlandinchristenhand_1039kps.mp4").getChannel());

        IsoFile out1 = new FragmentedMp4Builder().build(video);
        IsoFile out2 = new DefaultMp4Builder().build(video);


        FileChannel fc1 = new RandomAccessFile("output.fmp4", "rw").getChannel();
        fc1.position(0);
        out1.getBox(fc1);
        fc1.truncate(fc1.position());
        fc1.close();

        FileChannel fc2 = new RandomAccessFile("output.mp4", "rw").getChannel();
        fc2.position(0);
        out2.getBox(fc2);
        fc2.truncate(fc2.position());
        fc2.close();


    }


     */


}
