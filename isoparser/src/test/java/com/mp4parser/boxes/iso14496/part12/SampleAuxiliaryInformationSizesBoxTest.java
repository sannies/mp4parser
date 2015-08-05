package com.mp4parser.boxes.iso14496.part12;

import com.mp4parser.IsoFile;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class SampleAuxiliaryInformationSizesBoxTest {
    @Test
    public void roundTripFlags0() throws IOException {
        SampleAuxiliaryInformationSizesBox saiz1 = new SampleAuxiliaryInformationSizesBox();
        short[] ss = new short[]{1, 11, 10, 100};
        saiz1.setSampleInfoSizes(ss);
        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        FileChannel fc = new FileOutputStream(f).getChannel();
        saiz1.getBox(fc);
        fc.close();

        IsoFile isoFile = new IsoFile(new FileInputStream(f).getChannel());
        SampleAuxiliaryInformationSizesBox saiz2 = (SampleAuxiliaryInformationSizesBox) isoFile.getBoxes().get(0);

        Assert.assertEquals(saiz1.getDefaultSampleInfoSize(), saiz2.getDefaultSampleInfoSize());
        Assert.assertArrayEquals(saiz1.getSampleInfoSizes(), saiz2.getSampleInfoSizes());


    }

    @Test
    public void roundTripFlags1() throws IOException {
        SampleAuxiliaryInformationSizesBox saiz1 = new SampleAuxiliaryInformationSizesBox();
        saiz1.setFlags(1);
        saiz1.setAuxInfoType("piff");
        saiz1.setAuxInfoTypeParameter("trak");
        short[] ss = new short[]{1, 11, 10, 100};
        saiz1.setSampleInfoSizes(ss);
        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        f.deleteOnExit();
        FileChannel fc = new FileOutputStream(f).getChannel();
        saiz1.getBox(fc);
        fc.close();

        IsoFile isoFile = new IsoFile(new FileInputStream(f).getChannel());
        SampleAuxiliaryInformationSizesBox saiz2 = (SampleAuxiliaryInformationSizesBox) isoFile.getBoxes().get(0);

        Assert.assertEquals(saiz1.getDefaultSampleInfoSize(), saiz2.getDefaultSampleInfoSize());
        Assert.assertArrayEquals(saiz1.getSampleInfoSizes(), saiz2.getSampleInfoSizes());
        Assert.assertEquals(saiz1.getAuxInfoType(), saiz2.getAuxInfoType());
        Assert.assertEquals(saiz1.getAuxInfoTypeParameter(), saiz2.getAuxInfoTypeParameter());


    }
}
