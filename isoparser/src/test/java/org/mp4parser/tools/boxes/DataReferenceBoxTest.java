package org.mp4parser.tools.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.mp4parser.boxes.iso14496.part12.DataReferenceBox;
import org.mp4parser.boxes.iso14496.part12.FreeBox;

import java.util.Collections;
import java.util.Map;

/**
 * Created by sannies on 23.05.13.
 */
public class DataReferenceBoxTest extends BoxWriteReadBase<DataReferenceBox> {
    @Override
    public Class<DataReferenceBox> getBoxUnderTest() {
        return DataReferenceBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, DataReferenceBox box) {
        addPropsHere.put("boxes", Collections.singletonList(new FreeBox(100)));
    }
}
