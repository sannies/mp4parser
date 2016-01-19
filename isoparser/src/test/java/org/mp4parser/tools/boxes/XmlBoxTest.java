package org.mp4parser.tools.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.mp4parser.boxes.iso14496.part12.XmlBox;

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
