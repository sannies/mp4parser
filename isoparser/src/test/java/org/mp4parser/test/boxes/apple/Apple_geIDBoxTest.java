package org.mp4parser.test.boxes.apple;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.junit.Test;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.apple.Apple_geIDBox;
import org.mp4parser.tools.ByteBufferByteChannel;
import org.mp4parser.tools.Hex;

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
