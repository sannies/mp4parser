package com.googlecode.mp4parser.muxformats;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.AACTrackImpl;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created with IntelliJ IDEA.
 * User: magnus
 * Date: 2012-04-20
 * Time: 13:22
 * To change this template use File | Settings | File Templates.
 */
public class AacExample {
    public static void main(String[] args) throws IOException {
//        AACTrackImpl aacTrack = new AACTrackImpl(Ac3Example.class.getResourceAsStream("/sample.aac"));
        AACTrackImpl aacTrack = new AACTrackImpl(new FileDataSourceImpl("/Users/magnus/Projects/castlabs/cff/Solekai015_1920_29_75x75_v2/Solekai_BeautifulTension_15sec_160k.aac"));
        Movie m = new Movie();
        m.addTrack(aacTrack);
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        Container out = mp4Builder.build(m);
        FileOutputStream fos = new FileOutputStream("output.mp4");
        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);

        fos.close();
    }
}
