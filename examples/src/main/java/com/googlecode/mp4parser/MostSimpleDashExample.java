package com.googlecode.mp4parser;

import com.mp4parser.Container;
import com.mp4parser.muxer.FileRandomAccessSourceImpl;
import com.mp4parser.IsoFile;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.Mp4TrackImpl;
import com.mp4parser.muxer.builder.DefaultMp4Builder;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 11.05.13
 * Time: 17:54
 * To change this template use File | Settings | File Templates.
 */
public class MostSimpleDashExample {


    public static void main(String[] args) throws IOException {
        String basePath = GetDuration.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/dash/";

        Movie m = new Movie();
        IsoFile baseIsoFile = new IsoFile(basePath + "redbull_100kbit_dash.mp4");
        List<IsoFile> fragments = new LinkedList<IsoFile>();
        for (int i = 1; i < 9; i++) {
            fragments.add(new IsoFile(basePath + "redbull_10sec" + i + ".m4s"));
        }

        m.addTrack(new Mp4TrackImpl(1, new IsoFile("redbull_100kbit_dash.mp4"), new FileRandomAccessSourceImpl(new RandomAccessFile("redbull_100kbit_dash.mp4", "r")), "test"));


        DefaultMp4Builder builder = new DefaultMp4Builder();
        Container stdMp4 = builder.build(m);
        FileOutputStream fos = new FileOutputStream("out.mp4");
        stdMp4.writeContainer(fos.getChannel());
        fos.close();
    }
}
