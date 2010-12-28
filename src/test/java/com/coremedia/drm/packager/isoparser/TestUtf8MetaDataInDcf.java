package com.coremedia.drm.packager.isoparser;

import junit.framework.TestCase;
import com.coremedia.iso.IsoFile;

import java.io.IOException;

/**
 * Tests UTF-8 capability.
 */
public class TestUtf8MetaDataInDcf extends TestCase {
    public void testUtf8() throws IOException {

        IsoFile isoFile = new IsoFile(InputStreamIsoBufferHelper.get(getClass().getResourceAsStream("/file6141.odf"), -1));
        isoFile.parse();
        System.err.println(isoFile);
    }
}
