package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.util.Path;
import com.mp4parser.iso14496.part15.HevcConfigurationBox;
import junit.framework.TestCase;
import org.junit.Assert;

public class DashHelperTest extends TestCase {

    public void testGetRfc6381Codec() throws Exception {
        IsoFile isoFile = new IsoFile(DashHelperTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/foreman-hevc-384.mp4");

        SampleEntry sampleEntry = Path.getPath(isoFile, "/moov[0]/trak[0]/mdia[0]/minf[0]/stbl[0]/stsd[0]/hev1[0]");

        Assert.assertEquals("hev1.1.c.L93.80", DashHelper.getRfc6381Codec(sampleEntry));

    }
}