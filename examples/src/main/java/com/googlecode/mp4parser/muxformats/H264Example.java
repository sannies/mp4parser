package com.googlecode.mp4parser.muxformats;

import com.mp4parser.Container;
import com.mp4parser.muxer.FileDataSourceImpl;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.builder.DefaultMp4Builder;
import com.mp4parser.muxer.tracks.h264.H264TrackImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 * Created with IntelliJ IDEA.
 * User: magnus
 * Date: 2012-04-23
 * Time: 12:21
 * To change this template use File | Settings | File Templates.
 */
public class H264Example {
    public static void main(String[] args) throws IOException {
        H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl("C:\\dev\\mp4parser\\isoparser\\src\\test\\resources\\count.h264"));
        //AACTrackImpl aacTrack = new AACTrackImpl(new FileInputStream("/home/sannies2/Downloads/lv.aac").getChannel());
        Movie m = new Movie();
        m.addTrack(h264Track);

        //m.addTrack(aacTrack);

        {
            Container out = new DefaultMp4Builder().build(m);
            FileOutputStream fos = new FileOutputStream(new File("h264_output.mp4"));
            FileChannel fc = fos.getChannel();
            out.writeContainer(fc);
            fos.close();
        }
    }
}