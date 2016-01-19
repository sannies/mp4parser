package org.mp4parser.boxes.iso14496.part15;

import org.junit.Assert;
import org.junit.Test;
import org.mp4parser.tools.Hex;

import java.nio.ByteBuffer;

public class HevcDecoderConfigurationRecordTest {

    @Test
    public void roundtrip() {

        String example = "01008000000000000000000000F000FCFDF8F800000F03200001001940010C01FFFF00800000030000030000030000030000B50240210001002842010100800000030000030000030000030000A00280802D1FE5B59246D0CE4924B724AA49F292C822000100074401C1A5581E48";

        ByteBuffer confRecordOrig = ByteBuffer.wrap(Hex.decodeHex(example.replace(" ", "")));

        HevcDecoderConfigurationRecord h1 = new HevcDecoderConfigurationRecord();
        h1.parse(confRecordOrig);
        ByteBuffer confRecordWritten = ByteBuffer.allocate(h1.getSize());
        h1.write(confRecordWritten);

        HevcDecoderConfigurationRecord h2 = new HevcDecoderConfigurationRecord();
        h2.parse((ByteBuffer) confRecordWritten.rewind());

        Assert.assertEquals(confRecordOrig, confRecordWritten);
        Assert.assertEquals(h1, h2);


    }

}