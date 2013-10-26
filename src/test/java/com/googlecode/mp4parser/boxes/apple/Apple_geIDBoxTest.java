package com.googlecode.mp4parser.boxes.apple;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

/**
 * Created by sannies on 10/26/13.
 */
public class Apple_geIDBoxTest extends BoxWriteReadBase<Apple_geIDBox> {

    @Override
    public Class<Apple_geIDBox> getBoxUnderTest() {
        return Apple_geIDBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, Apple_geIDBox box) {
        addPropsHere.put("value", 1233l );
    }

    @Test
    public void testRealLifeBox() throws IOException {
        DataSource ds = new MemoryDataSourceImpl(Hex.decodeHex("0000001C67654944000000146461746100000015000000000000000A"));
        Apple_geIDBox geid = (Apple_geIDBox) new IsoFile(ds).getBoxes().get(0);
        System.err.println(geid.getValue());
    }
}
