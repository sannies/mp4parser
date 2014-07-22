package com.googlecode.mp4parser;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.FileTypeBox;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class DirectFileReadDataSourceTest {

    @Test
    public void testTempFileDeletion() throws Exception {
        InputStream resourceStream = DirectFileReadDataSourceTest.class.getResourceAsStream("/Beethoven - Bagatelle op.119 no.11 i.m4a");
        File temp = File.createTempFile(this.getClass().getSimpleName(), "m4a");
        FileOutputStream fos = new FileOutputStream(temp);
        IOUtils.copy(resourceStream, fos);
        resourceStream.close();
        fos.close();
        // using FileDataSourceImpl will keep the file locked on Windows
        // IsoFile isoFile = new IsoFile(new FileDataSourceImpl(temp));
        IsoFile isoFile = new IsoFile(new DirectFileReadDataSource(temp));
        FileTypeBox box = isoFile.getBoxes(FileTypeBox.class).get(0);
        // invoke parseDetails so DataSource#map is at least called once
        box.parseDetails();
        isoFile.close();
        Assert.assertTrue(temp.delete());
    }

}
