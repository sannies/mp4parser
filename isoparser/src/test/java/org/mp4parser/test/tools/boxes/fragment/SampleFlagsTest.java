package org.mp4parser.test.tools.boxes.fragment;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
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
        Assertions.assertEquals(l, IsoTypeReader.readUInt32(b));
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


        Assertions.assertEquals(sf.getReserved(), sf2.getReserved());
        Assertions.assertEquals(sf.getSampleDependsOn(), sf2.getSampleDependsOn());
        Assertions.assertEquals(sf.isSampleIsDifferenceSample(), sf2.isSampleIsDifferenceSample());
        Assertions.assertEquals(sf.getSamplePaddingValue(), sf2.getSamplePaddingValue());

        Assertions.assertEquals(sf.getSampleDegradationPriority(), sf2.getSampleDegradationPriority());
        Assertions.assertEquals(sf.getSampleHasRedundancy(), sf2.getSampleHasRedundancy());
        Assertions.assertEquals(sf.getSampleIsDependedOn(), sf2.getSampleIsDependedOn());

    }


}
