package org.mp4parser.test.tools;

import org.junit.jupiter.api.Test;
import org.mp4parser.IsoFile;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 *
 */
class IsoFileTest {

    @Test
    void testFourCC() {
        assertEquals("AA\0\0", IsoFile.bytesToFourCC(new byte[]{65, 65}));
        assertEquals("AAAA", IsoFile.bytesToFourCC(new byte[]{65, 65, 65, 65, 65, 65}));
        assertEquals("AAAA", new String(IsoFile.fourCCtoBytes("AAAAAAA")));
        assertEquals("AA\0\0", new String(IsoFile.fourCCtoBytes("AA")));
        assertEquals("\0\0\0\0", new String(IsoFile.fourCCtoBytes(null)));
        assertEquals("\0\0\0\0", new String(IsoFile.fourCCtoBytes("")));
        assertEquals("\0\0\0\0", IsoFile.bytesToFourCC(null));
        assertEquals("\0\0\0\0", IsoFile.bytesToFourCC(new byte[0]));
    }
}
