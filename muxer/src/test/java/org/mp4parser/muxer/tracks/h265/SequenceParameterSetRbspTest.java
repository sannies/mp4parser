package org.mp4parser.muxer.tracks.h265;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part15.HevcConfigurationBox;
import org.mp4parser.boxes.iso14496.part15.HevcDecoderConfigurationRecord;
import org.mp4parser.muxer.tracks.CleanInputStream;
import org.mp4parser.tools.Hex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

public class SequenceParameterSetRbspTest {
    @Test
    public void test1() throws IOException {
        byte[] hecCBytes = Hex.decodeHex("0000009068766343010220000000B0000000000096F000FCFDFAFA00000F03200001002040010C01FFFF022000000300B0000003000003009698903000003E900005DC052100010039420101022000000300B00000030000030096A001E020021C4D94626491B6BC05A84880482000007D20000BB80C25BDEFC0006C948000BEBC1022000100094401C1625B162C1ED9");
        HevcConfigurationBox hvcC = (HevcConfigurationBox) new IsoFile(Channels.newChannel(new ByteArrayInputStream(hecCBytes))).getBoxes().get(0);
        for (HevcDecoderConfigurationRecord.Array array : hvcC.getArrays()) {
            if (array.nal_unit_type == 33) {
                for (byte[] nalUnit : array.nalUnits) {
                    InputStream bais = new CleanInputStream(new ByteArrayInputStream(nalUnit));
                    bais.read(); // nal unit header
                    bais.read(); // nal unit header
                    SequenceParameterSetRbsp sps = new SequenceParameterSetRbsp(bais);
                    Assertions.assertTrue(sps.vuiParameters.colour_description_present_flag);
                    Assertions.assertEquals(9,sps.vuiParameters.colour_primaries);
                    Assertions.assertEquals(16,sps.vuiParameters.transfer_characteristics);
                    Assertions.assertEquals(9,sps.vuiParameters.matrix_coeffs);
                }
            }
        }

    }
}