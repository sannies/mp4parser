package com.googlecode.mp4parser.muxformats;

import com.mp4parser.tools.Hex;
import com.mp4parser.IsoFile;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.builder.DefaultMp4Builder;
import com.mp4parser.muxer.container.mp4.MovieCreator;
import com.mp4parser.muxer.tracks.mjpeg.OneJpegPerIframe;
import com.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import com.mp4parser.tools.Path;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

public class MjpegTest {
    public static void main(String[] args) throws IOException {
        IsoFile isofile = new IsoFile("C:\\content\\bbb-small\\output_320x180-mjpeg.mp4");
        ESDescriptorBox esDescriptorBox = Path.getPath(isofile, "/moov[0]/trak[0]/mdia[0]/minf[0]/stbl[0]/stsd[0]/mp4v[0]/esds[0]");
        byte[] d = new byte[esDescriptorBox.getData().rewind().remaining()];
        esDescriptorBox.getData().get(d);
        System.err.println(Hex.encodeHex(d));

        Movie mRef = MovieCreator.build("C:\\content\\bbb-small\\output_320x180_150.mp4");
        Track refTrack = mRef.getTracks().get(0);

        File baseDir = new File("C:\\content\\bbb-small");
        File[] iFrameJpegs = baseDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jpg");
            }
        });
        Arrays.sort(iFrameJpegs);

        Movie mRes = new Movie();
        mRes.addTrack(new OneJpegPerIframe("iframes", iFrameJpegs, refTrack));

        new DefaultMp4Builder().build(mRes).writeContainer(new FileOutputStream("output-mjpeg.mp4").getChannel());
    }
}
