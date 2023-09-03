package org.mp4parser.test.tools.boxes.fragment;

import org.junit.Assert;
import org.junit.Test;
import org.mp4parser.boxes.iso14496.part12.SampleFlags;
import org.mp4parser.tools.IsoTypeReader;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 *
 */
public class SampleFlagsTest {
    @Test
    public void testSimple() throws IOException {
        long l = 0x11223344;
        SampleFlags sf = new SampleFlags(ByteBuffer.wrap(new byte[]{0x11, 0x22, 0x33, 0x44}));
        ByteBuffer b = ByteBuffer.allocate(4);
        sf.getContent(b);
        ((Buffer)b).rewind();
        Assert.assertEquals(l, IsoTypeReader.readUInt32(b));
    }

    @Test
    public void testSetterGetterRoundTrip() throws IOException {
        SampleFlags sf = new SampleFlags();
        sf.setReserved(1);
        sf.setSampleDegradationPriority(1);
        sf.setSampleDependsOn(1);
        sf.setSampleHasRedundancy(2);
        sf.setSampleIsDependedOn(3);
        sf.setSampleIsDifferenceSample(true);
        sf.setSamplePaddingValue(3);
        ByteBuffer bb = ByteBuffer.allocate(4);
        sf.getContent(bb);
        ((Buffer)bb).rewind();
        //System.err.println(BitWriterBufferTest.toString(bb));
        SampleFlags sf2 = new SampleFlags(bb);


        Assert.assertEquals(sf.getReserved(), sf2.getReserved());
        Assert.assertEquals(sf.getSampleDependsOn(), sf2.getSampleDependsOn());
        Assert.assertEquals(sf.isSampleIsDifferenceSample(), sf2.isSampleIsDifferenceSample());
        Assert.assertEquals(sf.getSamplePaddingValue(), sf2.getSamplePaddingValue());

        Assert.assertEquals(sf.getSampleDegradationPriority(), sf2.getSampleDegradationPriority());
        Assert.assertEquals(sf.getSampleHasRedundancy(), sf2.getSampleHasRedundancy());
        Assert.assertEquals(sf.getSampleIsDependedOn(), sf2.getSampleIsDependedOn());

    }


}
