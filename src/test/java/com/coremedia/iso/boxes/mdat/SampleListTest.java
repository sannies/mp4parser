package com.coremedia.iso.boxes.mdat;


import com.coremedia.drm.packager.isoparser.InputStreamIsoBufferHelper;
import com.coremedia.iso.*;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.TrackBox;
import junitx.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.hamcrest.core.Is;
import org.junit.Test;

import javax.print.attribute.standard.Media;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class SampleListTest {
    @Test
    public void testGotAll() throws IOException {
        File originalFile = File.createTempFile("SampleListTest", "testGotAll");
        FileOutputStream fos = new FileOutputStream(originalFile);
        byte[] content = IOUtils.toByteArray(getClass().getResourceAsStream("/Beethoven - Bagatelle op.119 no.11 i.m4a"));
        fos.write(content);
        fos.close();
        IsoFile isoFile = new IsoFile(new IsoBufferWrapperImpl(content));
        isoFile.parse();

        TrackBox tb = isoFile.getBoxes(MovieBox.class).get(0).getBoxes(TrackBox.class).get(0);
        SampleList sl = new SampleList(tb);

        MediaDataBox mdat = isoFile.getBoxes(MediaDataBox.class).get(0);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mdat.getContent(new IsoOutputStream(baos));
        byte[] contentOfMdat = baos.toByteArray();
        long currentOffset = 0;
        for (IsoBufferWrapper isoBufferWrapper : sl) {
            long remaining = isoBufferWrapper.remaining();
            while (remaining > 0) {
                byte ist = isoBufferWrapper.read();
                byte soll = contentOfMdat[((int) currentOffset)];
                if (soll != ist) {
                    System.err.println("Offset " + currentOffset + " soll: " + soll + " ist: " + ist);
                }
                Assert.assertEquals(soll, ist);
                currentOffset++;
                remaining--;
            }

        }
    }
}
