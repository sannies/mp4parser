package com;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.fragment.TrackRunBox;
import com.googlecode.mp4parser.util.Path;

import java.io.IOException;

/**
 * Created by sannies on 28.05.2015.
 */
public class Inspect {
    public static void main(String[] args) throws IOException {
        IsoFile isoFile  = new IsoFile("C:\\Users\\sannies\\sound-b.mp4");
        TrackRunBox trun = Path.getPath(isoFile, "/moof[0]/traf[0]/trun[0]");
        int i = 0;
        long duration = 0;
        for (TrackRunBox.Entry entry : trun.getEntries()) {
            duration += (entry.getSampleDuration());
            i++;
        }
        System.err.println((double)duration / i);
    }
}
