package com.googlecode.mp4parser.boxes;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.googlecode.mp4parser.ByteBufferByteChannel;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

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
    public void testRoundTripFlagsZero() throws IOException {
        List<AbstractSampleEncryptionBox.Entry> entries = new LinkedList<AbstractSampleEncryptionBox.Entry>();
        AbstractSampleEncryptionBox.Entry entry = new AbstractSampleEncryptionBox.Entry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entries.add(entry);

        senc.setEntries(entries);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        long sizeBeforeWrite = senc.getSize();
        senc.getBox(new IsoOutputStream(baos));
        Assert.assertEquals(baos.size(), senc.getSize());
        Assert.assertEquals(baos.size(), sizeBeforeWrite);
        IsoFile iso = new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray())));


        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractSampleEncryptionBox);
        AbstractSampleEncryptionBox senc2 = (AbstractSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(0, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));

    }

    @Test
    public void testRoundTripFlagsOne() throws IOException {
        senc.setAlgorithmId(0x333333);
        senc.setIvSize(8);
        senc.setKid(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8,});

        List<AbstractSampleEncryptionBox.Entry> entries = new LinkedList<AbstractSampleEncryptionBox.Entry>();
        AbstractSampleEncryptionBox.Entry entry = new AbstractSampleEncryptionBox.Entry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entries.add(entry);

        senc.setEntries(entries);
        long sizeBeforeWrite = senc.getSize();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        senc.getBox(new IsoOutputStream(baos));
        Assert.assertEquals(baos.size(), senc.getSize());
        Assert.assertEquals(sizeBeforeWrite, senc.getSize());
        IsoFile iso = new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray())));

        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractSampleEncryptionBox);
        AbstractSampleEncryptionBox senc2 = (AbstractSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(1, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));
    }

    @Test
    public void testRoundTripFlagsTwo() throws IOException {
        List<AbstractSampleEncryptionBox.Entry> entries = new LinkedList<AbstractSampleEncryptionBox.Entry>();
        AbstractSampleEncryptionBox.Entry entry = new AbstractSampleEncryptionBox.Entry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entry.pairs.add(new AbstractSampleEncryptionBox.Entry.Pair(5, 15));
        entry.pairs.add(new AbstractSampleEncryptionBox.Entry.Pair(5, 16));
        entry.pairs.add(new AbstractSampleEncryptionBox.Entry.Pair(5, 17));
        entry.pairs.add(new AbstractSampleEncryptionBox.Entry.Pair(5, 18));
        entry.pairs.add(new AbstractSampleEncryptionBox.Entry.Pair(5, 19));
        entries.add(entry);


        senc.setEntries(entries);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        senc.getBox(new IsoOutputStream(baos));
        Assert.assertEquals(baos.size(), senc.getSize());
        IsoFile iso = new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray())));

        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractSampleEncryptionBox);
        AbstractSampleEncryptionBox senc2 = (AbstractSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(2, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));

    }

    @Test
    public void testRoundTripFlagsThree() throws IOException {
        senc.setAlgorithmId(0x333333);
        senc.setIvSize(8);
        senc.setKid(new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 1, 2, 3, 4, 5, 6, 7, 8,});
        List<AbstractSampleEncryptionBox.Entry> entries = new LinkedList<AbstractSampleEncryptionBox.Entry>();
        AbstractSampleEncryptionBox.Entry entry = new AbstractSampleEncryptionBox.Entry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entry.pairs.add(new AbstractSampleEncryptionBox.Entry.Pair(5, 15));
        entry.pairs.add(new AbstractSampleEncryptionBox.Entry.Pair(5, 16));
        entry.pairs.add(new AbstractSampleEncryptionBox.Entry.Pair(5, 17));
        entry.pairs.add(new AbstractSampleEncryptionBox.Entry.Pair(5, 18));
        entry.pairs.add(new AbstractSampleEncryptionBox.Entry.Pair(5, 19));
        entries.add(entry);
        entries.add(entry);
        entries.add(entry);
        entries.add(entry);

        senc.setEntries(entries);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        senc.getBox(new IsoOutputStream(baos));

        Assert.assertEquals(baos.size(), senc.getSize());

        IsoFile iso = new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray())));

        Assert.assertTrue(iso.getBoxes().get(0) instanceof AbstractSampleEncryptionBox);
        AbstractSampleEncryptionBox senc2 = (AbstractSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(3, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));
    }
}
