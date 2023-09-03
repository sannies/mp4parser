package org.mp4parser.test.tools.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.mp4parser.boxes.iso14496.part12.FreeBox;
import org.mp4parser.boxes.iso14496.part12.SampleTableBox;

import java.util.Collections;
import java.util.Map;

/**
 * Created by sannies on 23.05.13.
 */
public class SampleTableBoxTest extends BoxWriteReadBase<SampleTableBox> {
    @Override
    public Class<SampleTableBox> getBoxUnderTest() {
        return SampleTableBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, SampleTableBox box) {
        addPropsHere.put("boxes", Collections.singletonList(new FreeBox(100)));
    }
}
