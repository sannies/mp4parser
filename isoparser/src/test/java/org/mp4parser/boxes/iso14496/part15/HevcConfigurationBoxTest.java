package org.mp4parser.boxes.iso14496.part15;

import org.junit.Test;
import org.mp4parser.IsoFile;
import org.mp4parser.tools.ByteBufferByteChannel;
import org.mp4parser.tools.Hex;
import org.mp4parser.tools.Path;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;

import static org.junit.Assert.assertArrayEquals;

public class HevcConfigurationBoxTest {

    byte[] in = Hex.decodeHex("000000E1687663310000000000000001000000000000000000000000000000000780043800480000004800000000000000010C0B4845564320436F64696E67000000000000000000000000000000000000000018FFFF0000008B68766343010200000001B0000000000096F000FCFDFAFA00000F03A00001002040010C01FFFF02A000000300B0000003000003009694903000003E900005DC05A10001003542010102A000000300B00000030000030096A003C08010E4D94526491B6BC040400000FA4000177018077BDF8000C95A000192B420A2000100084401C1625B6C1ED9");

    @Test
    public void testInOutIdent() throws Exception {
        IsoFile isoFile = new IsoFile(new ByteBufferByteChannel(in));
        HevcConfigurationBox hevC = Path.getPath(isoFile, "hvc1/hvcC");
        assert hevC != null;
        hevC.parseDetails();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        isoFile.getBox(Channels.newChannel(baos));
        assertArrayEquals(in, baos.toByteArray());
    }
}