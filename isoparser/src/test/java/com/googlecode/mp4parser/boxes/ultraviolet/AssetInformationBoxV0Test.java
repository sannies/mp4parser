package com.googlecode.mp4parser.boxes.ultraviolet;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.googlecode.mp4parser.boxes.dece.AssetInformationBox;

import java.util.Map;


public class AssetInformationBoxV0Test extends BoxWriteReadBase<AssetInformationBox> {

    @Override
    public Class<AssetInformationBox> getBoxUnderTest() {
        return AssetInformationBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> values, AssetInformationBox box) {
        values.put("version", 0);
        values.put("v0Apid", "urn:dece:apid:com:drmtoday:beta:12345abcdef");
        values.put("v0ProfileVersion", "hdv1");
    }

}
