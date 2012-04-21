package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

public class SampleAuxiliaryInformationSizesBoxTest {
    @Test
    public void roundTripFlags0() throws IOException {
        SampleAuxiliaryInformationSizesBox saiz1 = new SampleAuxiliaryInformationSizesBox();
        List<Short> ss = new LinkedList<Short>();
        ss.add((short)1);
        ss.add((short)11);
        ss.add((short)10);
        ss.add((short)100);
        saiz1.setSampleInfoSizes(ss);
        ByteBuffer bb = ByteBuffer.allocate(l2i(saiz1.getSize()));
        saiz1.getBox(new ByteBufferByteChannel(bb));
        Assert.assertTrue(bb.remaining() == 0);
        bb.rewind();

        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(bb));
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
        ss.add((short)1);
        ss.add((short)11);
        ss.add((short)10);
        ss.add((short)100);
        saiz1.setSampleInfoSizes(ss);
        ByteBuffer bb = ByteBuffer.allocate(l2i(saiz1.getSize()));
        saiz1.getBox(new ByteBufferByteChannel(bb));
        Assert.assertTrue(bb.remaining() == 0);
        bb.rewind();

        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(bb));
        SampleAuxiliaryInformationSizesBox saiz2 = (SampleAuxiliaryInformationSizesBox) isoFile.getBoxes().get(0);

        Assert.assertEquals(saiz1.getDefaultSampleInfoSize(), saiz2.getDefaultSampleInfoSize());
        Assert.assertEquals(saiz1.getSampleInfoSizes(), saiz2.getSampleInfoSizes());
        Assert.assertEquals(saiz1.getAuxInfoType(), saiz2.getAuxInfoType());
        Assert.assertEquals(saiz1.getAuxInfoTypeParameter(), saiz2.getAuxInfoTypeParameter());
        Assert.assertEquals(saiz1.getSampleInfoSizes(), saiz2.getSampleInfoSizes());


    }
}
