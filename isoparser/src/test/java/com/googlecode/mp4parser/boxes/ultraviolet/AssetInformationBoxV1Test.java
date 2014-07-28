package com.googlecode.mp4parser.boxes.ultraviolet;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.googlecode.mp4parser.boxes.dece.AssetInformationBox;

import java.util.Arrays;
import java.util.Map;


public class AssetInformationBoxV1Test extends BoxWriteReadBase<AssetInformationBox> {

    @Override
    public Class<AssetInformationBox> getBoxUnderTest() {
        return AssetInformationBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> values, AssetInformationBox box) {
        values.put("version", 1);
        values.put("v1Codecs", (String)"mp4v.20.9" );
        values.put("v1Encrypted", (boolean) true );
        values.put("v1Entries", Arrays.asList(new AssetInformationBox.Entry("a", "b", "c"), new AssetInformationBox.Entry("a", "b", "c2")));
        values.put("v1Hidden", (boolean) true );
        values.put("v1MimeSubtypeName", (String)"video/vnd.dece.video" );

    }

}
