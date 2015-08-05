package com;

import com.mp4parser.IsoFile;
import com.mp4parser.boxes.iso14496.part12.TrackRunBox;
import com.mp4parser.tools.Path;

import java.io.IOException;

public class Inspect {
    public static void main(String[] args) throws IOException {
        IsoFile isoFile  = new IsoFile("C:\\Users\\sannies\\sound-b.mp4");
        TrackRunBox trun = Path.getPath(isoFile, "/moof[0]/traf[0]/trun[0]");
        int i = 0;
        long duration = 0;
        assert trun != null;
        for (TrackRunBox.Entry entry : trun.getEntries()) {
            duration += (entry.getSampleDuration());
            i++;
        }
        System.err.println((double)duration / i);
    }
}
