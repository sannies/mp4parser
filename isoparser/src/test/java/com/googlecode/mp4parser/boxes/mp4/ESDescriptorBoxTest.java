package com.googlecode.mp4parser.boxes.mp4;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.googlecode.mp4parser.util.Path;
import org.junit.Test;

/**
 * Created by sannies on 20.05.2015.
 */
public class ESDescriptorBoxTest {
    @Test
    public void testEsDescriptor() throws Exception {
        String esdsBytes = "0000002A6573647300000000031C000000041440150018000001F4000001F4000505131056E598060102";
        //String esdsBytes = "0000003365736473000000000380808022000200048080801440150000000006AD650006AD65058080800211B0068080800102";
        IsoFile isoFile = new IsoFile(new MemoryDataSourceImpl(Hex.decodeHex(esdsBytes)));
        ESDescriptorBox esds = Path.getPath(isoFile, "esds");
        System.err.println(esds.getEsDescriptor());

    }
}