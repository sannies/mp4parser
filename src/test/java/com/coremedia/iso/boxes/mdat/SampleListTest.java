package com.coremedia.iso.boxes.mdat;


import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

public class SampleListTest {
    ByteBuffer getMdatContent(FileChannel fc) throws IOException {

        while (fc.size() - fc.position() > 8) {
            long start = fc.position();
            ByteBuffer bb = ByteBuffer.allocate(8);
            fc.read(bb);
            bb.rewind();
            long size = IsoTypeReader.readUInt32(bb);
            String type = IsoTypeReader.read4cc(bb);
            long end = start + size;
            if (type.equals("mdat")) {
                ByteBuffer mdatContent = ByteBuffer.allocate(l2i(size));
                fc.read(mdatContent);
                mdatContent.rewind();
                return mdatContent;
            }


            fc.position(end);

        }
        Assert.fail("No mdat found!?!");
        return null;
    }

    @Test
    public void testGotAll() throws IOException {
        File originalFile = File.createTempFile("SampleListTest", "testGotAll");
        FileOutputStream fos = new FileOutputStream(originalFile);
        byte[] content = IOUtils.toByteArray(getClass().getResourceAsStream("/Beethoven - Bagatelle op.119 no.11 i.m4a"));
        fos.write(content);
        fos.close();

        IsoFile isoFile = new IsoFile(new RandomAccessFile(originalFile, "r").getChannel());

        TrackBox tb = isoFile.getBoxes(MovieBox.class).get(0).getBoxes(TrackBox.class).get(0);
        SampleList sl = new SampleList(tb);
        ByteBuffer mdatContent = getMdatContent(new RandomAccessFile(originalFile, "r").getChannel());

        for (ByteBuffer sample : sl) {

            while (sample.remaining() > 0) {
                byte ist = sample.get();
                byte soll = mdatContent.get();
                Assert.assertEquals("Offset " + mdatContent.position() + " soll: " + soll + " ist: " + ist, soll, ist);
            }

        }
        Assert.assertTrue(originalFile.delete());
    }

    @Test
    public void testGotAllWithMappingFailed() throws IOException {
        MediaDataBox.FAKE_MAPPING_FAIL = true;
        File originalFile = File.createTempFile("SampleListTest", "testGotAllWithMappingFailed");
        FileOutputStream fos = new FileOutputStream(originalFile);
        byte[] content = IOUtils.toByteArray(getClass().getResourceAsStream("/Beethoven - Bagatelle op.119 no.11 i.m4a"));
        fos.write(content);
        fos.close();
        IsoFile isoFile = new IsoFile(new RandomAccessFile(originalFile, "r").getChannel());

        TrackBox tb = isoFile.getBoxes(MovieBox.class).get(0).getBoxes(TrackBox.class).get(0);
        SampleList sl = new SampleList(tb);

        ByteBuffer mdatContent = getMdatContent(new RandomAccessFile(originalFile, "r").getChannel());
        for (ByteBuffer sample : sl) {

            while (sample.remaining() > 0) {
                byte ist = sample.get();
                byte soll = mdatContent.get();
                Assert.assertEquals("Offset " + mdatContent.position() + " soll: " + soll + " ist: " + ist, soll, ist);
            }

        }
        Assert.assertTrue(originalFile.delete());
    }
}
