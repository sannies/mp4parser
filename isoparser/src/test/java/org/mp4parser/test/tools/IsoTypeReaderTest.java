package org.mp4parser.test.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.IsoTypeWriter;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Test symmetrie of IsoBufferWrapper and Iso
 */
class IsoTypeReaderTest {

    @Test
    void testInt() throws IOException {
        ByteBuffer bb = ByteBuffer.allocate(20);

        IsoTypeWriter.writeUInt8(bb, 0);
        IsoTypeWriter.writeUInt8(bb, 255);
        IsoTypeWriter.writeUInt16(bb, 0);
        IsoTypeWriter.writeUInt16(bb, (1 << 16) - 1);
        IsoTypeWriter.writeUInt24(bb, 0);
        IsoTypeWriter.writeUInt24(bb, (1 << 24) - 1);
        IsoTypeWriter.writeUInt32(bb, 0);
        IsoTypeWriter.writeUInt32(bb, (1L << 32) - 1);
        ((Buffer)bb).rewind();

        Assertions.assertEquals(0, IsoTypeReader.readUInt8(bb));
        Assertions.assertEquals(255, IsoTypeReader.readUInt8(bb));
        Assertions.assertEquals(0, IsoTypeReader.readUInt16(bb));
        Assertions.assertEquals((1 << 16) - 1, IsoTypeReader.readUInt16(bb));
        Assertions.assertEquals(0, IsoTypeReader.readUInt24(bb));
        Assertions.assertEquals((1 << 24) - 1, IsoTypeReader.readUInt24(bb));
        Assertions.assertEquals(0, IsoTypeReader.readUInt32(bb));
        Assertions.assertEquals((1L << 32) - 1, IsoTypeReader.readUInt32(bb));
    }

    @Test
    void testFixedPoint1616() throws IOException {
        final double fixedPointTest1 = 10.13;
        final double fixedPointTest2 = -10.13;

        ByteBuffer bb = ByteBuffer.allocate(8);

        IsoTypeWriter.writeFixedPoint1616(bb, fixedPointTest1);
        IsoTypeWriter.writeFixedPoint1616(bb, fixedPointTest2);
        ((Buffer)bb).rewind();

        Assertions.assertEquals(fixedPointTest1, IsoTypeReader.readFixedPoint1616(bb), 1d / 65536, "fixedPointTest1");
        Assertions.assertEquals(fixedPointTest2, IsoTypeReader.readFixedPoint1616(bb), 1d / 65536, "fixedPointTest2");
    }

    @Test
    void testFixedPoint0230() throws IOException {
        final double fixedPointTest1 = 1.13;
        final double fixedPointTest2 = -1.13;

        ByteBuffer bb = ByteBuffer.allocate(8);

        IsoTypeWriter.writeFixedPoint0230(bb, fixedPointTest1);
        IsoTypeWriter.writeFixedPoint0230(bb, fixedPointTest2);
        ((Buffer)bb).rewind();

        Assertions.assertEquals(fixedPointTest1, IsoTypeReader.readFixedPoint0230(bb), 1d / 65536, "fixedPointTest1");
        Assertions.assertEquals(fixedPointTest2, IsoTypeReader.readFixedPoint0230(bb), 1d / 65536, "fixedPointTest2");
    }

    @Test
    void testFixedPoint88() throws IOException {
        final double fixedPointTest1 = 10.13;
        final double fixedPointTest2 = -10.13;
        ByteBuffer bb = ByteBuffer.allocate(4);

        IsoTypeWriter.writeFixedPoint88(bb, fixedPointTest1);
        IsoTypeWriter.writeFixedPoint88(bb, fixedPointTest2);
        ((Buffer)bb).rewind();

        Assertions.assertEquals(fixedPointTest1, IsoTypeReader.readFixedPoint88(bb), 1d / 256, "fixedPointTest1");
        Assertions.assertEquals(fixedPointTest2, IsoTypeReader.readFixedPoint88(bb), 1d / 256, "fixedPointTest2");
    }

    @Test
    void testRead4cc() throws IOException {
        ByteBuffer bb = ByteBuffer.wrap("abcd".getBytes());
        String code = IsoTypeReader.read4cc(bb);
        Assertions.assertEquals(4, bb.position());
        Assertions.assertEquals("abcd", code);
    }
}
