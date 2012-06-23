package com.googlecode.mp4parser.boxes.ultraviolet;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.HashMap;
import java.util.Map;


public class AssetInformationBoxTest extends BoxWriteReadBase<AssetInformationBox> {

    @Override
    public Class<AssetInformationBox> getBoxUnderTest() {
        return AssetInformationBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> values) {
        values.put("apid", "urn:dece:apid:com:drmtoday:beta:12345abcdef");
        values.put("profileVersion", "hdv1");
    }

}
