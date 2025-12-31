package org.mp4parser.test.tools;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mp4parser.IsoFile;
import org.mp4parser.tools.Path;

import java.io.FileInputStream;
import java.io.IOException;

public class PathTest {
    IsoFile isoFile;

    @BeforeEach
    public void setup() throws IOException {
        isoFile = new IsoFile(new FileInputStream(PathTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/multiTrack.3gp").getChannel());
    }


    @Test
    public void testComponentMatcher() {
        Assertions.assertTrue(Path.component.matcher("abcd").matches());
        Assertions.assertTrue(Path.component.matcher("xml ").matches());
        Assertions.assertTrue(Path.component.matcher("xml [1]").matches());
        Assertions.assertTrue(Path.component.matcher("..").matches());
    }
}
