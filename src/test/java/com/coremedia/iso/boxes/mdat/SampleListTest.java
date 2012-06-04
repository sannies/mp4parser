package com.coremedia.iso.boxes.mdat;


import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

public class SampleListTest {
    @Test
    public void testGotAll() throws IOException {
        File originalFile = File.createTempFile("SampleListTest", "testGotAll");
        FileOutputStream fos = new FileOutputStream(originalFile);
        byte[] content = IOUtils.toByteArray(getClass().getResourceAsStream("/Beethoven - Bagatelle op.119 no.11 i.m4a"));
        fos.write(content);
        fos.close();
        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(content)));

        TrackBox tb = isoFile.getBoxes(MovieBox.class).get(0).getBoxes(TrackBox.class).get(0);
        SampleList sl = new SampleList(tb);

        MediaDataBox mdat = isoFile.getBoxes(MediaDataBox.class).get(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] contentOfMdat = new byte[mdat.getContent().remaining()];
        mdat.getContent().get(contentOfMdat);
        long currentOffset = 0;
        for (ByteBuffer sample : sl) {

            while (sample.remaining() > 0) {
                byte ist = sample.get();
                byte soll = contentOfMdat[((int) currentOffset)];
                if (soll != ist) {
                    System.err.println("Offset " + currentOffset + " soll: " + soll + " ist: " + ist);
                }
                Assert.assertEquals(soll, ist);
                currentOffset++;
            }

        }
        originalFile.delete();
    }

    @Test
    public void testGotAllWithMappingFailed() throws IOException {
        MediaDataBox.FAKE_MAPPING_FAIL = true;
        File originalFile = File.createTempFile("SampleListTest", "testGotAllWithMappingFailed");
        FileOutputStream fos = new FileOutputStream(originalFile);
        byte[] content = IOUtils.toByteArray(getClass().getResourceAsStream("/Beethoven - Bagatelle op.119 no.11 i.m4a"));
        fos.write(content);
        fos.close();
        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(content)));

        TrackBox tb = isoFile.getBoxes(MovieBox.class).get(0).getBoxes(TrackBox.class).get(0);
        SampleList sl = new SampleList(tb);

        MediaDataBox mdat = isoFile.getBoxes(MediaDataBox.class).get(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] contentOfMdat = new byte[mdat.getContent().remaining()];
        mdat.getContent().get(contentOfMdat);
        long currentOffset = 0;
        for (ByteBuffer sample : sl) {

            while (sample.remaining() > 0) {
                byte ist = sample.get();
                byte soll = contentOfMdat[((int) currentOffset)];
                if (soll != ist) {
                    System.err.println("Offset " + currentOffset + " soll: " + soll + " ist: " + ist);
                }
                Assert.assertEquals(soll, ist);
                currentOffset++;
            }

        }
        originalFile.delete();
    }
}
