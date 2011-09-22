package com.googlecode.mp4parser.h264;

import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoBufferWrapperImpl;
import com.googlecode.mp4parser.h264.model.NALUnit;
import junit.framework.Assert;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

/**
 *
 */
public class AccessUnitSourceImplTest {
    @Test
    public void testNextAccessUnit() throws Exception {
        //IsoBufferWrapper ibw = new IsoBufferWrapperImpl(new File("/home/sannies/suckerpunch-samurai_h640w_track1.h264"));
        IsoBufferWrapper ibw = new IsoBufferWrapperImpl(IOUtils.toByteArray(NALUnitReaderTest.class.getResourceAsStream("/count.h264")));
        AnnexBNALUnitReader nalUnitReader = new AnnexBNALUnitReader(ibw);
        AccessUnitSource accessUnitSource = new AccessUnitSourceImpl(nalUnitReader);
        AccessUnit au;
        int count = 0;
        while ((au = accessUnitSource.nextAccessUnit()) != null) {
            //System.err.println("New AU");

            IsoBufferWrapper nal;
            while ((nal = au.nextNALUnit()) != null) {
                //nal.position(0);
                NALUnit nu = NALUnit.read(nal);
                //System.err.println(nu);
            }
            count++;
        }
        Assert.assertEquals(245, count);
    }
}
