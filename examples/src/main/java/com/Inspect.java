package com;

import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.TrackRunBox;
import org.mp4parser.tools.Path;

import java.io.IOException;
import java.util.logging.LogManager;

public class Inspect {

    public static void main(String[] args) throws IOException {
        //IsoFile isoFile  = new IsoFile("C:\\Users\\sannies\\media-144384.mp4");
        LogManager.getLogManager().readConfiguration(Inspect.class.getResourceAsStream("/log.properties"));
        IsoFile isoFile = new IsoFile("C:\\dev\\mp4parser\\output.mp4");
        TrackRunBox trun = Path.getPath(isoFile, "moof[0]/traf[0]/trun[0]");
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
