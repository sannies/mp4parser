package org.mp4parser.test.tools;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mp4parser.IsoFile;
import org.mp4parser.tools.Path;

import java.io.FileInputStream;
import java.io.IOException;

public class PathTest {
    IsoFile isoFile;

    @Before
    public void setup() throws IOException {
        try (FileInputStream fis = new FileInputStream(getClass().getClassLoader().getResource("multiTrack.3gp").getPath())) {
            isoFile = new IsoFile(fis.getChannel());
        }
    }


    @Test
    public void testComponentMatcher() {
        Assert.assertTrue(Path.component.matcher("abcd").matches());
        Assert.assertTrue(Path.component.matcher("xml ").matches());
        Assert.assertTrue(Path.component.matcher("xml [1]").matches());
        Assert.assertTrue(Path.component.matcher("..").matches());
    }
}
