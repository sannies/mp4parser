package com.mp4parser.iso14496.part15;

import com.coremedia.iso.Hex;
import org.junit.Assert;
import org.junit.Test;

import java.nio.ByteBuffer;

import static org.junit.Assert.*;

public class HEVCDecoderConfigurationRecordTest {

    @Test
    public void roundtrip() {
        String example = "00000000-00000001-00000000-00000000-00000000-00000000-050002D0-00480000-00480000-00000000-" +
                "00010000-00000000-00000000-00000000-00000000-00000000-00000000-00000000-00000018-FFFF0000-00696876-" +
                "63430101-60000000-80000000-00005DF0-00FCFDF8-F800000F-03A00001-00184001-0C01FFFF-01600000-03008000-" +
                "00030000-03005DB5-0240A100-01001D42-01010160-00000300-80000003-00000300-5DA00280-802D165B-5BBCAC80-" +
                "A2000100-064401C0-71831200-00001462-74727400-011CB800-2402C000-12FF00";
        ByteBuffer confRecordOrig = ByteBuffer.wrap(Hex.decodeHex(example.replace("-", "")));

        HEVCDecoderConfigurationRecord h1 = new HEVCDecoderConfigurationRecord();
        h1.parse(confRecordOrig);
        ByteBuffer confRecordWritten = ByteBuffer.allocate(h1.getSize());
        h1.write(confRecordWritten);

        HEVCDecoderConfigurationRecord h2 = new HEVCDecoderConfigurationRecord();
        h2.parse((ByteBuffer) confRecordWritten.rewind());

        Assert.assertEquals(h1, h2);
        Assert.assertEquals(confRecordOrig, confRecordWritten);




    }

}