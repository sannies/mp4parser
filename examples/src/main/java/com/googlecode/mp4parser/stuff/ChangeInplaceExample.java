package com.googlecode.mp4parser.stuff;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.HandlerBox;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.util.Path;
import org.apache.commons.lang.RandomStringUtils;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Collections;

public class ChangeInplaceExample {
    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        DataSource rChannel = new FileDataSourceImpl("/media/scratch/CSI.S12E21.HDTV.x264-LOL.mp4");
        FileChannel wChannel = new RandomAccessFile("/media/scratch/ThreeHundredFourtyThreeMB_2.mp4", "rw").getChannel();
        IsoFile isoFile = new IsoFile(rChannel);
        HandlerBox hdlr = (HandlerBox) Path.getPath(isoFile, "/moov[0]/trak[0]/mdia[0]/hdlr[0]");
        hdlr.setName(RandomStringUtils.random(hdlr.getName().length()));
        isoFile.getBox(wChannel);
        rChannel.close();
        wChannel.close();
        System.err.println((System.currentTimeMillis() - start) + "ms");
        new File("/media/scratch/ThreeHundredFourtyThreeMB_2.mp4").delete();
    }
}
