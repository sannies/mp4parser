package com.googlecode.mp4parser.muxformats;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.mjpeg.OneJpegPerIframe;
import com.googlecode.mp4parser.boxes.mp4.ESDescriptorBox;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by sannies on 13.02.2015.
 */
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
