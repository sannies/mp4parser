package com.googlecode.mp4parser.util;


import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.TrackBox;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.channels.Channels;

public class PathTest {
    IsoFile isoFile;
    Path path;

    @Before
    public void setup() throws IOException {
        isoFile = new IsoFile(Channels.newChannel(PathTest.class.getResourceAsStream("/multiTrack.3gp")));
        path = new Path(isoFile);
    }

    @Test
    public void testRoundTrip() throws IOException {
        Box b1 = isoFile.getMovieBox().getBoxes(TrackBox.class).get(1).getSampleTableBox().getTimeToSampleBox();
        String p = path.createPath(b1);
        Box b2 = path.getPath(p);
        Assert.assertSame(b1, b2);
    }
}
