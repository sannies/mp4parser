package com;


import org.mp4parser.IsoFile;
import org.mp4parser.muxer.FileRandomAccessSourceImpl;
import org.mp4parser.muxer.samples.FragmentedMp4SampleList;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by sannies on 28.05.2015.
 */
public class Inspect {
    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile("C:\\Users\\sannies\\Downloads\\gesamt.mp4");
        FragmentedMp4SampleList f = new FragmentedMp4SampleList(1, isoFile, new FileRandomAccessSourceImpl(new RandomAccessFile("C:\\Users\\sannies\\Downloads\\gesamt.mp4", "r")));
        System.err.println(f.get(0));
    }
}
