package org.mp4parser.boxes.iso14496.part12;

import org.junit.Assert;
import org.junit.Test;
import org.mp4parser.IsoFile;
import org.mp4parser.tools.ByteBufferByteChannel;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ItemLocationBoxTest {

    int[] v = new int[]{1, 2, 4, 8};

    @Test
    public void testSimpleRoundTrip() throws IOException {
        for (int i : v) {
            for (int i1 : v) {
                for (int i2 : v) {
                    for (int i3 : v) {
                        testSimpleRoundTrip(i, i1, i2, i3);
                    }
                }
            }
        }

    }

    public void testSimpleRoundTrip(int baseOffsetSize, int indexSize, int lengthSize, int offsetSize) throws IOException {
        ItemLocationBox ilocOrig = new ItemLocationBox();
        ilocOrig.setVersion(1);
        ilocOrig.setBaseOffsetSize(baseOffsetSize);
        ilocOrig.setIndexSize(indexSize);
        ilocOrig.setLengthSize(lengthSize);
        ilocOrig.setOffsetSize(offsetSize);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();


        ilocOrig.getBox(Channels.newChannel(baos));


        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(baos.toByteArray()));

        ItemLocationBox iloc = (ItemLocationBox) isoFile.getBoxes().get(0);

        Assert.assertEquals(ilocOrig.getBaseOffsetSize(), iloc.getBaseOffsetSize());
        Assert.assertEquals(ilocOrig.getContentSize(), iloc.getContentSize());
        Assert.assertEquals(ilocOrig.getIndexSize(), iloc.getIndexSize());
        Assert.assertEquals(ilocOrig.getLengthSize(), iloc.getLengthSize());
        Assert.assertEquals(ilocOrig.getOffsetSize(), iloc.getOffsetSize());
        Assert.assertEquals(ilocOrig.getItems(), iloc.getItems());


    }


    @Test
    public void testSimpleRoundWithEntriesTrip() throws IOException {
        for (int i : v) {
            for (int i1 : v) {
                for (int i2 : v) {
                    for (int i3 : v) {
                        testSimpleRoundWithEntriesTrip(i, i1, i2, i3);
                    }
                }
            }
        }
    }

    public void testSimpleRoundWithEntriesTrip(int baseOffsetSize, int indexSize, int lengthSize, int offsetSize) throws IOException {
        ItemLocationBox ilocOrig = new ItemLocationBox();
        ilocOrig.setVersion(1);
        ilocOrig.setBaseOffsetSize(baseOffsetSize);
        ilocOrig.setIndexSize(indexSize);
        ilocOrig.setLengthSize(lengthSize);
        ilocOrig.setOffsetSize(offsetSize);
        ItemLocationBox.Item item = ilocOrig.createItem(12, 0, 13, 123, Collections.<ItemLocationBox.Extent>emptyList());
        ilocOrig.setItems(Collections.singletonList(item));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ilocOrig.getBox(Channels.newChannel(baos));

        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(baos.toByteArray()));

        ItemLocationBox iloc = (ItemLocationBox) isoFile.getBoxes().get(0);

        Assert.assertEquals(ilocOrig.getBaseOffsetSize(), iloc.getBaseOffsetSize());
        Assert.assertEquals(ilocOrig.getContentSize(), iloc.getContentSize());
        Assert.assertEquals(ilocOrig.getIndexSize(), iloc.getIndexSize());
        Assert.assertEquals(ilocOrig.getLengthSize(), iloc.getLengthSize());
        Assert.assertEquals(ilocOrig.getOffsetSize(), iloc.getOffsetSize());
        Assert.assertEquals(ilocOrig.getItems(), iloc.getItems());


    }

    @Test
    public void testSimpleRoundWithEntriesAndExtentsTrip() throws IOException {
        for (int i : v) {
            for (int i1 : v) {
                for (int i2 : v) {
                    for (int i3 : v) {
                        testSimpleRoundWithEntriesAndExtentsTrip(i, i1, i2, i3);
                    }
                }
            }
        }
    }

    public void testSimpleRoundWithEntriesAndExtentsTrip(int baseOffsetSize, int indexSize, int lengthSize, int offsetSize) throws IOException {
        ItemLocationBox ilocOrig = new ItemLocationBox();
        ilocOrig.setVersion(1);
        ilocOrig.setBaseOffsetSize(baseOffsetSize);
        ilocOrig.setIndexSize(indexSize);
        ilocOrig.setLengthSize(lengthSize);
        ilocOrig.setOffsetSize(offsetSize);
        List<ItemLocationBox.Extent> extents = new LinkedList<ItemLocationBox.Extent>();
        ItemLocationBox.Extent extent = ilocOrig.createExtent(12, 13, 1);
        extents.add(extent);
        ItemLocationBox.Item item = ilocOrig.createItem(12, 0, 13, 123, extents);
        ilocOrig.setItems(Collections.singletonList(item));
        File f = File.createTempFile(this.getClass().getSimpleName(), "");
        f.deleteOnExit();
        FileChannel fc = new FileOutputStream(f).getChannel();
        ilocOrig.getBox(fc);
        fc.close();


        IsoFile isoFile = new IsoFile(new FileInputStream(f).getChannel());

        ItemLocationBox iloc = (ItemLocationBox) isoFile.getBoxes().get(0);

        Assert.assertEquals(ilocOrig.getBaseOffsetSize(), iloc.getBaseOffsetSize());
        Assert.assertEquals(ilocOrig.getContentSize(), iloc.getContentSize());
        Assert.assertEquals(ilocOrig.getIndexSize(), iloc.getIndexSize());
        Assert.assertEquals(ilocOrig.getLengthSize(), iloc.getLengthSize());
        Assert.assertEquals(ilocOrig.getOffsetSize(), iloc.getOffsetSize());
        Assert.assertEquals(ilocOrig.getItems(), iloc.getItems());


    }

    @Test
    public void testExtent() throws IOException {
        testExtent(1, 2, 4, 8);
        testExtent(2, 4, 8, 1);
        testExtent(4, 8, 1, 2);
        testExtent(8, 1, 2, 4);
    }

    public void testExtent(int a, int b, int c, int d) throws IOException {
        ItemLocationBox iloc = new ItemLocationBox();
        iloc.setVersion(1);
        iloc.setBaseOffsetSize(a);
        iloc.setIndexSize(b);
        iloc.setLengthSize(c);
        iloc.setOffsetSize(d);
        ItemLocationBox.Extent e1 = iloc.createExtent(123, 124, 125);
        ByteBuffer bb = ByteBuffer.allocate(e1.getSize());
        e1.getContent(bb);
        Assert.assertTrue(bb.remaining() == 0);
        bb.rewind();
        ItemLocationBox.Extent e2 = iloc.createExtent(bb);

        Assert.assertEquals(e1, e2);


    }

    @Test
    public void testItem() throws IOException {
        testItem(1, 2, 4, 8);
        testItem(2, 4, 8, 1);
        testItem(4, 8, 1, 2);
        testItem(8, 1, 2, 4);
    }

    public void testItem(int a, int b, int c, int d) throws IOException {
        ItemLocationBox iloc = new ItemLocationBox();
        iloc.setVersion(1);
        iloc.setBaseOffsetSize(a);
        iloc.setIndexSize(b);
        iloc.setLengthSize(c);
        iloc.setOffsetSize(d);
        ItemLocationBox.Item e1 = iloc.createItem(65, 1, 0, 66, Collections.<ItemLocationBox.Extent>emptyList());
        ByteBuffer bb = ByteBuffer.allocate(e1.getSize());
        e1.getContent(bb);
        Assert.assertTrue(bb.remaining() == 0);
        bb.rewind();
        ItemLocationBox.Item e2 = iloc.createItem(bb);

        Assert.assertEquals(e1, e2);


    }

    @Test
    public void testItemVersionZero() throws IOException {
        testItemVersionZero(1, 2, 4, 8);
        testItemVersionZero(2, 4, 8, 1);
        testItemVersionZero(4, 8, 1, 2);
        testItemVersionZero(8, 1, 2, 4);
    }

    public void testItemVersionZero(int a, int b, int c, int d) throws IOException {
        ItemLocationBox iloc = new ItemLocationBox();
        iloc.setVersion(0);
        iloc.setBaseOffsetSize(a);
        iloc.setIndexSize(b);
        iloc.setLengthSize(c);
        iloc.setOffsetSize(d);
        ItemLocationBox.Item e1 = iloc.createItem(65, 0, 1, 66, Collections.<ItemLocationBox.Extent>emptyList());
        ByteBuffer bb = ByteBuffer.allocate(e1.getSize());
        e1.getContent(bb);
        Assert.assertTrue(bb.remaining() == 0);
        bb.rewind();
        ItemLocationBox.Item e2 = iloc.createItem(bb);

        Assert.assertEquals(e1, e2);


    }
}
