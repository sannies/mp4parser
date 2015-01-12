package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.sampleentry.SampleEntry;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.boxes.apple.PixelAspectRationAtom;
import com.googlecode.mp4parser.util.Path;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sannies on 11.01.2015.
 */
public class RemovePasp {
    public static void main(String[] args) throws IOException {
        IsoFile i = new IsoFile("C:\\Users\\sannies\\Downloads\\CF2.0_1920x1080_8000.mp4");

        VisualSampleEntry sampleEntry = Path.getPath(i, "/moov[0]/trak[0]/mdia[0]/minf[0]/stbl[0]/stsd[0]/avc1[0]");
        List<Box> nuBoxes = new ArrayList<Box>();
        for (Box box : sampleEntry.getBoxes()) {
            if (!box.getType().equals("pasp")) {
                nuBoxes.add(box);
            }
        }
        sampleEntry.setBoxes(nuBoxes);


        i.writeContainer(new FileOutputStream("C:\\Users\\sannies\\Downloads\\CF2.0_1920x1080_8000-without-pasp.mp4").getChannel());
    }

}
