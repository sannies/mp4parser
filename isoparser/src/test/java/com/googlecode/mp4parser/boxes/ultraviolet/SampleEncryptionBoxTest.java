package com.googlecode.mp4parser.boxes.ultraviolet;

import com.mp4parser.IsoFile;
import com.mp4parser.boxes.iso23001.part7.AbstractSampleEncryptionBox;
import com.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import com.mp4parser.boxes.iso23001.part7.SampleEncryptionBox;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;


public class SampleEncryptionBoxTest  {


    SampleEncryptionBox senc = new SampleEncryptionBox();


    @Test
    public void testRoundTripFlagsZero() throws IOException {
        List<CencSampleAuxiliaryDataFormat> entries = new LinkedList<CencSampleAuxiliaryDataFormat>();

        CencSampleAuxiliaryDataFormat entry = new CencSampleAuxiliaryDataFormat();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entries.add(entry);

        senc.setEntries(entries);

        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        f.deleteOnExit();
        FileChannel fc = new FileOutputStream(f).getChannel();
        senc.getBox(fc);
        fc.close();
        Assert.assertEquals(f.length(), senc.getSize());


        IsoFile iso = new IsoFile(new FileInputStream(f).getChannel());


        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractSampleEncryptionBox);
        AbstractSampleEncryptionBox senc2 = (AbstractSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(0, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));

    }


    @Test
    public void testRoundTripFlagsTwo() throws IOException {
        senc.setSubSampleEncryption(true);
        List<CencSampleAuxiliaryDataFormat> entries = new LinkedList<CencSampleAuxiliaryDataFormat>();
        CencSampleAuxiliaryDataFormat entry = new CencSampleAuxiliaryDataFormat();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entry.pairs = new CencSampleAuxiliaryDataFormat.Pair[5];
        entry.pairs[0]=entry.createPair(5, 15);
        entry.pairs[1]=entry.createPair(5, 16);
        entry.pairs[2]=entry.createPair(5, 17);
        entry.pairs[3]=entry.createPair(5, 18);
        entry.pairs[4]=entry.createPair(5, 19);
        entries.add(entry);


        senc.setEntries(entries);

        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        FileChannel fc = new FileOutputStream(f).getChannel();
        senc.getBox(fc);
        fc.close();

        IsoFile iso = new IsoFile(new FileInputStream(f).getChannel());

        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractSampleEncryptionBox);
        AbstractSampleEncryptionBox senc2 = (AbstractSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(2, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));

    }


}
