package com;


import org.mp4parser.Box;
import org.mp4parser.IsoFile;
import org.mp4parser.muxer.FileRandomAccessSourceImpl;
import org.mp4parser.muxer.container.mp4.FragmentedMp4SampleList;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by sannies on 28.05.2015.
 */
public class Inspect {
    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile("/Users/sannies/dev/mp4parser/tos_vp9.MP4");
        // FragmentedMp4SampleList f = new FragmentedMp4SampleList(0, isoFile, new FileRandomAccessSourceImpl(new RandomAccessFile("/Users/sannies/dev/mp4parser/tos_vp9.MP4", "r")));
        for (Box box : isoFile.getBoxes()) {
            System.err.println(box);
        }

    }
}
