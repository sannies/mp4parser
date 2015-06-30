package com.mp4parser.iso14496.part15;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.googlecode.mp4parser.util.Path;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;

import static org.junit.Assert.*;

/**
 * Created by sannies on 27.06.2015.
 */
public class HevcConfigurationBoxTest {

    byte[] in = Hex.decodeHex("000000E1687663310000000000000001000000000000000000000000000000000780043800480000004800000000000000010C0B4845564320436F64696E67000000000000000000000000000000000000000018FFFF0000008B68766343010200000001B0000000000096F000FCFDFAFA00000F03A00001002040010C01FFFF02A000000300B0000003000003009694903000003E900005DC05A10001003542010102A000000300B00000030000030096A003C08010E4D94526491B6BC040400000FA4000177018077BDF8000C95A000192B420A2000100084401C1625B6C1ED9");

    @Test
    public void testInOutIdent() throws Exception {
        IsoFile isoFile = new IsoFile(new MemoryDataSourceImpl(in));
        HevcConfigurationBox hevC = Path.getPath(isoFile, "hvc1/hvcC");
        hevC.parseDetails();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        isoFile.getBox(Channels.newChannel(baos));
        assertArrayEquals(in, baos.toByteArray());
    }
}