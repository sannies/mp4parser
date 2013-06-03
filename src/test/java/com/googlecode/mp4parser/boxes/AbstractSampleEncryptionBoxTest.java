
package com.googlecode.mp4parser.boxes;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.util.UUIDConverter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 11/19/11
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractSampleEncryptionBoxTest {
    protected AbstractSampleEncryptionBox senc;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testRoundTripSmallIV() throws IOException {
        List<AbstractSampleEncryptionBox.Entry> entries = new LinkedList<AbstractSampleEncryptionBox.Entry>();
        senc.setOverrideTrackEncryptionBoxParameters(true);
        senc.setAlgorithmId(1);
        senc.setIvSize(8);
        senc.setKid(UUIDConverter.convert(UUID.randomUUID()));
        AbstractSampleEncryptionBox.Entry entry = senc.createEntry();
        entry.iv = new byte[]{1, 2, 3, 5, 6, 7, 8};
        entries.add(entry);
        entry = senc.createEntry();
        entry.iv = new byte[]{1, 2, 3, 5, 6, 7, 9};
        entries.add(entry);
        entry = senc.createEntry();
        entry.iv = new byte[]{1, 2, 3, 5, 6, 7, 10};
        entries.add(entry);

        senc.setEntries(entries);

        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        FileChannel fc = new FileOutputStream(f).getChannel();
        senc.getBox(fc);
        fc.close();

        Assert.assertEquals(f.length(), senc.getSize());
        IsoFile iso = new IsoFile(f.getAbsolutePath());


        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractSampleEncryptionBox);
        AbstractSampleEncryptionBox senc2 = (AbstractSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(senc.getFlags(), senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));

    }

    @Test
    public void testRoundTripFlagsZero() throws IOException {
        List<AbstractSampleEncryptionBox.Entry> entries = new LinkedList<AbstractSampleEncryptionBox.Entry>();

        AbstractSampleEncryptionBox.Entry entry = senc.createEntry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entries.add(entry);

        senc.setEntries(entries);

        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        FileChannel fc = new FileOutputStream(f).getChannel();
        senc.getBox(fc);
        fc.close();
        Assert.assertEquals(f.length(), senc.getSize());


        IsoFile iso = new IsoFile(f.getAbsolutePath());


        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractSampleEncryptionBox);
        AbstractSampleEncryptionBox senc2 = (AbstractSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(0, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));

    }

    @Test
    public void testRoundTripFlagsOne() throws IOException {
        senc.setOverrideTrackEncryptionBoxParameters(true);
        senc.setAlgorithmId(0x333333);
        senc.setIvSize(8);
        senc.setKid(UUIDConverter.convert(UUID.randomUUID()));

        List<AbstractSampleEncryptionBox.Entry> entries = new LinkedList<AbstractSampleEncryptionBox.Entry>();
        AbstractSampleEncryptionBox.Entry entry = senc.createEntry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entries.add(entry);

        senc.setEntries(entries);
        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        FileChannel fc = new FileOutputStream(f).getChannel();
        senc.getBox(fc);
        fc.close();

        IsoFile iso = new IsoFile(f.getAbsolutePath());

        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractSampleEncryptionBox);
        AbstractSampleEncryptionBox senc2 = (AbstractSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(1, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));
    }

    @Test
    public void testRoundTripFlagsTwo() throws IOException {
        senc.setSubSampleEncryption(true);
        List<AbstractSampleEncryptionBox.Entry> entries = new LinkedList<AbstractSampleEncryptionBox.Entry>();
        AbstractSampleEncryptionBox.Entry entry = senc.createEntry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entry.pairs = new LinkedList<AbstractSampleEncryptionBox.Entry.Pair>();
        entry.pairs.add(entry.createPair(5, 15));
        entry.pairs.add(entry.createPair(5, 16));
        entry.pairs.add(entry.createPair(5, 17));
        entry.pairs.add(entry.createPair(5, 18));
        entry.pairs.add(entry.createPair(5, 19));
        entries.add(entry);


        senc.setEntries(entries);

        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        FileChannel fc = new FileOutputStream(f).getChannel();
        senc.getBox(fc);
        fc.close();

        IsoFile iso = new IsoFile(f.getAbsolutePath());

        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractSampleEncryptionBox);
        AbstractSampleEncryptionBox senc2 = (AbstractSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(2, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));

    }

    @Test
    public void testRoundTripFlagsThree() throws IOException {
        senc.setSubSampleEncryption(true);
        senc.setOverrideTrackEncryptionBoxParameters(true);
        senc.setAlgorithmId(0x333333);
        senc.setIvSize(8);
        senc.setKid(UUIDConverter.convert(UUID.randomUUID()));
        List<AbstractSampleEncryptionBox.Entry> entries = new LinkedList<AbstractSampleEncryptionBox.Entry>();
        AbstractSampleEncryptionBox.Entry entry = senc.createEntry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entry.pairs = new LinkedList<AbstractSampleEncryptionBox.Entry.Pair>();
        entry.pairs.add(entry.createPair(5, 15));
        entry.pairs.add(entry.createPair(5, 16));
        entry.pairs.add(entry.createPair(5, 17));
        entry.pairs.add(entry.createPair(5, 18));
        entry.pairs.add(entry.createPair(5, 19));
        entries.add(entry);
        entries.add(entry);
        entries.add(entry);
        entries.add(entry);

        senc.setEntries(entries);

        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        FileChannel fc = new FileOutputStream(f).getChannel();
        senc.getBox(fc);
        fc.close();

        IsoFile iso = new IsoFile(f.getAbsolutePath());

        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractSampleEncryptionBox);
        AbstractSampleEncryptionBox senc2 = (AbstractSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(3, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));
    }
}
