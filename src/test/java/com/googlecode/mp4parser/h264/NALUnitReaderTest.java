package com.googlecode.mp4parser.h264;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 *
 */
public class NALUnitReaderTest {
    @Test
    public void testNextNALUnit() throws Exception {
        IsoBufferWrapper ibw = new IsoBufferWrapperImpl(IOUtils.toByteArray(NALUnitReaderTest.class.getResourceAsStream("/count.h264")));
        AnnexBNALUnitReader nalUnitReader = new AnnexBNALUnitReader(ibw);
        IsoBufferWrapper nal;
        int count = 0;
        while ((nal = nalUnitReader.nextNALUnit()) != null) {
            count++;
        }
        Assert.assertEquals(248, count);
    }
}
