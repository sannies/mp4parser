package com;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;

import java.io.FileOutputStream;
import java.io.IOException;

public class MuxTest {
    public static void main(String[] args) throws IOException {
        Movie m = new Movie();
        Track t = new H264TrackImpl(new FileDataSourceImpl("C:\\Users\\sannies\\Documents\\mubi_fps2\\288p_400kbps_4.h264"));
        m.addTrack(t);
        Container c2 = new FragmentedMp4Builder().build(m);
        c2.writeContainer(new FileOutputStream("b.mp4").getChannel());
       /* Container c1 = new DefaultMp4Builder().build(m);
        c1.writeContainer(new FileOutputStream("a.mp4").getChannel());*/
    }
}