package com.coremedia.iso.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;

import java.util.Map;

/**
 *
 */
public class XmlBoxTest extends BoxWriteReadBase<XmlBox> {

    @Override
    public Class<XmlBox> getBoxUnderTest() {
        return XmlBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, XmlBox box) {
        addPropsHere.put("xml", "<a></a>");
    }

}
