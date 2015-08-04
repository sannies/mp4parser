package com.googlecode.mp4parser.boxes.apple;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class Apple_geIDBoxTest extends BoxWriteReadBase<Apple_geIDBox> {

    @Override
    public Class<Apple_geIDBox> getBoxUnderTest() {
        return Apple_geIDBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, Apple_geIDBox box) {
        addPropsHere.put("value", 1233l);
    }

    @Test
    public void testRealLifeBox() throws IOException {
        Apple_geIDBox geid = (Apple_geIDBox) new IsoFile(new ByteBufferByteChannel(Hex.decodeHex("0000001C67654944000000146461746100000015000000000000000A"))).getBoxes().get(0);
        System.err.println(geid.getValue());
    }
}
