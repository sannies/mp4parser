package com.googlecode.mp4parser.boxes.piff;

import com.coremedia.iso.IsoBufferWrapperImpl;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.fail;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 11/19/11
 * Time: 4:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserTypePiffSampleEncryptionBoxTest {


    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testRoundTripFlagsZero() throws IOException {
        UserTypePiffSampleEncryptionBox senc = new UserTypePiffSampleEncryptionBox();
        List<UserTypePiffSampleEncryptionBox.Entry> entries = new LinkedList<UserTypePiffSampleEncryptionBox.Entry>();
        UserTypePiffSampleEncryptionBox.Entry entry = new UserTypePiffSampleEncryptionBox.Entry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entries.add(entry);

        senc.setEntries(entries);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        senc.getBox(new IsoOutputStream(baos));

        IsoFile iso = new IsoFile(new IsoBufferWrapperImpl(baos.toByteArray()));
        iso.parse();

        Assert.assertTrue(iso.getBoxes().get(0) instanceof UserTypePiffSampleEncryptionBox);
        UserTypePiffSampleEncryptionBox senc2 = (UserTypePiffSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(0, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));

    }

    @Test
    public void testRoundTripFlagsOne() throws IOException {
        UserTypePiffSampleEncryptionBox senc = new UserTypePiffSampleEncryptionBox();
        senc.setAlgorithmId(0x333333);
        senc.setIvSize(8);
        senc.setKid(new byte[]{1,2,3,4,5,6,7,8,1,2,3,4,5,6,7,8,});
        
        List<UserTypePiffSampleEncryptionBox.Entry> entries = new LinkedList<UserTypePiffSampleEncryptionBox.Entry>();
        UserTypePiffSampleEncryptionBox.Entry entry = new UserTypePiffSampleEncryptionBox.Entry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entries.add(entry);

        senc.setEntries(entries);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        senc.getBox(new IsoOutputStream(baos));

        IsoFile iso = new IsoFile(new IsoBufferWrapperImpl(baos.toByteArray()));
        iso.parse();

        Assert.assertTrue(iso.getBoxes().get(0) instanceof UserTypePiffSampleEncryptionBox);
        UserTypePiffSampleEncryptionBox senc2 = (UserTypePiffSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(1, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));
    }

    @Test
    public void testRoundTripFlagsTwo() throws IOException {
        UserTypePiffSampleEncryptionBox senc = new UserTypePiffSampleEncryptionBox();
        List<UserTypePiffSampleEncryptionBox.Entry> entries = new LinkedList<UserTypePiffSampleEncryptionBox.Entry>();
        UserTypePiffSampleEncryptionBox.Entry entry = new UserTypePiffSampleEncryptionBox.Entry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entry.pairs.add(new UserTypePiffSampleEncryptionBox.Entry.Pair(5,15));
        entry.pairs.add(new UserTypePiffSampleEncryptionBox.Entry.Pair(5,16));
        entry.pairs.add(new UserTypePiffSampleEncryptionBox.Entry.Pair(5,17));
        entry.pairs.add(new UserTypePiffSampleEncryptionBox.Entry.Pair(5,18));
        entry.pairs.add(new UserTypePiffSampleEncryptionBox.Entry.Pair(5,19));
        entries.add(entry);


        senc.setEntries(entries);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        senc.getBox(new IsoOutputStream(baos));

        IsoFile iso = new IsoFile(new IsoBufferWrapperImpl(baos.toByteArray()));
        iso.parse();

        Assert.assertTrue(iso.getBoxes().get(0) instanceof UserTypePiffSampleEncryptionBox);
        UserTypePiffSampleEncryptionBox senc2 = (UserTypePiffSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(2, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));

    }

    @Test
    public void testRoundTripFlagsThree() throws IOException {
        UserTypePiffSampleEncryptionBox senc = new UserTypePiffSampleEncryptionBox();
        senc.setAlgorithmId(0x333333);
        senc.setIvSize(8);
        senc.setKid(new byte[]{1,2,3,4,5,6,7,8,1,2,3,4,5,6,7,8,});
        List<UserTypePiffSampleEncryptionBox.Entry> entries = new LinkedList<UserTypePiffSampleEncryptionBox.Entry>();
        UserTypePiffSampleEncryptionBox.Entry entry = new UserTypePiffSampleEncryptionBox.Entry();
        entry.iv = new byte[]{1, 2, 3, 4, 5, 6, 7, 8};
        entry.pairs.add(new UserTypePiffSampleEncryptionBox.Entry.Pair(5,15));
        entry.pairs.add(new UserTypePiffSampleEncryptionBox.Entry.Pair(5,16));
        entry.pairs.add(new UserTypePiffSampleEncryptionBox.Entry.Pair(5,17));
        entry.pairs.add(new UserTypePiffSampleEncryptionBox.Entry.Pair(5,18));
        entry.pairs.add(new UserTypePiffSampleEncryptionBox.Entry.Pair(5,19));
        entries.add(entry);

        senc.setEntries(entries);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        senc.getBox(new IsoOutputStream(baos));

        IsoFile iso = new IsoFile(new IsoBufferWrapperImpl(baos.toByteArray()));
        iso.parse();

        Assert.assertTrue(iso.getBoxes().get(0) instanceof UserTypePiffSampleEncryptionBox);
        UserTypePiffSampleEncryptionBox senc2 = (UserTypePiffSampleEncryptionBox) iso.getBoxes().get(0);
        Assert.assertEquals(3, senc2.getFlags());
        Assert.assertTrue(senc.equals(senc2));
        Assert.assertTrue(senc2.equals(senc));
    }
}
