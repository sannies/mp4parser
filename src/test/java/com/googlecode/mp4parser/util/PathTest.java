package com.googlecode.mp4parser.util;


import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.TrackBox;
import com.googlecode.mp4parser.FileDataSourceImpl;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

public class PathTest {
    IsoFile isoFile;

    @Before
    public void setup() throws IOException {
        isoFile = new IsoFile(new FileDataSourceImpl(PathTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/multiTrack.3gp"));
    }

    @Test
    public void testRoundTrip() throws IOException {
        Box b1 = isoFile.getMovieBox().getBoxes(TrackBox.class).get(1).getSampleTableBox().getTimeToSampleBox();
        String p = Path.createPath(b1);
        Box b2 = Path.getPath(isoFile, p);
        Assert.assertSame(b1, b2);
    }

    @Test
    public void testGetParent() throws Exception {
        Box b1 = isoFile.getMovieBox().getBoxes(TrackBox.class).get(1).getSampleTableBox().getTimeToSampleBox();
        Assert.assertEquals(isoFile.getMovieBox().getBoxes(TrackBox.class).get(1).getSampleTableBox(), Path.getPath(b1, ".."));

    }

    @Test
    public void testComponentMatcher() {
        Assert.assertTrue(Path.component.matcher("abcd").matches());
        Assert.assertTrue(Path.component.matcher("xml ").matches());
        Assert.assertTrue(Path.component.matcher("xml [1]").matches());
        Assert.assertTrue(Path.component.matcher("..").matches());
    }
}
