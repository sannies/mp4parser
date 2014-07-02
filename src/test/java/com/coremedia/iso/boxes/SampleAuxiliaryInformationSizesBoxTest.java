package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.DataSource;
import junit.framework.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class SampleAuxiliaryInformationSizesBoxTest {
    @Test
    public void roundTripFlags0() throws IOException {
        SampleAuxiliaryInformationSizesBox saiz1 = new SampleAuxiliaryInformationSizesBox();
        List<Short> ss = new LinkedList<Short>();
        ss.add((short) 1);
        ss.add((short) 11);
        ss.add((short) 10);
        ss.add((short) 100);
        saiz1.setSampleInfoSizes(ss);
        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        FileChannel fc = new FileOutputStream(f).getChannel();
        saiz1.getBox(fc);
        fc.close();

        IsoFile isoFile = new IsoFile(f.getAbsolutePath());
        SampleAuxiliaryInformationSizesBox saiz2 = (SampleAuxiliaryInformationSizesBox) isoFile.getBoxes().get(0);

        Assert.assertEquals(saiz1.getDefaultSampleInfoSize(), saiz2.getDefaultSampleInfoSize());
        Assert.assertEquals(saiz1.getSampleInfoSizes(), saiz2.getSampleInfoSizes());


    }

    @Test
    public void roundTripFlags1() throws IOException {
        SampleAuxiliaryInformationSizesBox saiz1 = new SampleAuxiliaryInformationSizesBox();
        saiz1.setFlags(1);
        saiz1.setAuxInfoType("piff");
        saiz1.setAuxInfoTypeParameter("trak");
        List<Short> ss = new LinkedList<Short>();
        ss.add((short) 1);
        ss.add((short) 11);
        ss.add((short) 10);
        ss.add((short) 100);
        saiz1.setSampleInfoSizes(ss);
        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        f.deleteOnExit();
        FileChannel fc = new FileOutputStream(f).getChannel();
        saiz1.getBox(fc);
        fc.close();

        IsoFile isoFile = new IsoFile(f.getAbsolutePath());
        SampleAuxiliaryInformationSizesBox saiz2 = (SampleAuxiliaryInformationSizesBox) isoFile.getBoxes().get(0);

        Assert.assertEquals(saiz1.getDefaultSampleInfoSize(), saiz2.getDefaultSampleInfoSize());
        Assert.assertEquals(saiz1.getSampleInfoSizes(), saiz2.getSampleInfoSizes());
        Assert.assertEquals(saiz1.getAuxInfoType(), saiz2.getAuxInfoType());
        Assert.assertEquals(saiz1.getAuxInfoTypeParameter(), saiz2.getAuxInfoTypeParameter());
        Assert.assertEquals(saiz1.getSampleInfoSizes(), saiz2.getSampleInfoSizes());


    }
}
