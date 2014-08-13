package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Mp4TrackImpl;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;

import java.io.FileOutputStream;
import java.io.IOException;
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

        m.addTrack(new Mp4TrackImpl("redbull_100kbit_dash.mp4",
                baseIsoFile.getMovieBox().getBoxes(TrackBox.class).get(0),
                fragments.toArray(new IsoFile[fragments.size()])));


        DefaultMp4Builder builder = new DefaultMp4Builder();
        Container stdMp4 = builder.build(m);
        FileOutputStream fos = new FileOutputStream("out.mp4");
        stdMp4.writeContainer(fos.getChannel());
        fos.close();
    }
}
