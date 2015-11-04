package com;


import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.SampleSizeBox;
import org.mp4parser.tools.Path;

import java.io.IOException;

/**
 * Created by sannies on 28.05.2015.
 */
public class Inspect {
    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile("C:\\Users\\sannies\\Downloads\\mergedvideo.mp4");
        SampleSizeBox stsz = Path.getPath(isoFile, "/moov[0]/trak[1]/mdia[0]/minf[0]/stbl[0]/stsz[0]");
        long s = 0;

        for (long l : stsz.getSampleSizes()) {
            s += l;
        }
        System.err.println(s);
    }
}
