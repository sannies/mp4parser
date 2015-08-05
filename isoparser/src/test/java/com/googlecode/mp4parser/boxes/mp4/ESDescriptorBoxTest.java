package com.googlecode.mp4parser.boxes.mp4;

import com.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import com.mp4parser.tools.Hex;
import com.mp4parser.IsoFile;
import com.mp4parser.tools.ByteBufferByteChannel;
import com.mp4parser.tools.Path;
import org.junit.Test;

public class ESDescriptorBoxTest {
    @Test
    public void testEsDescriptor() throws Exception {
        String esdsBytes = "0000002A6573647300000000031C000000041440150018000001F4000001F4000505131056E598060102";
        //String esdsBytes = "0000003365736473000000000380808022000200048080801440150000000006AD650006AD65058080800211B0068080800102";
        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(Hex.decodeHex(esdsBytes)));
        ESDescriptorBox esds = Path.getPath(isoFile, "esds");
        assert esds != null;
        System.err.println(esds.getEsDescriptor());

    }
}