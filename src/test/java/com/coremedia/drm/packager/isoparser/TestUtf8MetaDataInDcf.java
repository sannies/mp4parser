package com.coremedia.drm.packager.isoparser;

import junit.framework.TestCase;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.ByteArrayRandomAccessDataSource;

import java.io.IOException;

/**
 * Tests UTF-8 capability.
 */
public class TestUtf8MetaDataInDcf extends TestCase {
    public void testUtf8() throws IOException {
        IsoFile isoFile = new IsoFile(new ByteArrayRandomAccessDataSource(getClass().getResourceAsStream("/file6141.odf")));
        isoFile.parse();
        System.err.println(isoFile);
    }
}
