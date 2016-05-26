package com;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.MetaBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.googlecode.mp4parser.util.Path;

import java.io.IOException;

/**
 * Created by sannies on 28.05.2015.
 */
public class Inspect {
    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile("C:\\Users\\sannies\\Downloads\\mp4parser-151.mp4");
        SampleSizeBox stsz = Path.getPath(isoFile, "/moov[0]/trak[1]/mdia[0]/minf[0]/stbl[0]/stsz[0]");
        MetaBox meta = Path.getPath(isoFile, "/moov[0]/meta");
        long s = 0;
        for (Box box : meta.getBoxes()) {
            System.err.println(box);
        }

        for (long l : stsz.getSampleSizes()) {
            s += l;
        }
        System.err.println(s);
    }
}
