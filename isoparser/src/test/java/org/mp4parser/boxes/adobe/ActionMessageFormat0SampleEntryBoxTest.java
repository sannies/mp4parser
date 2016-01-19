package org.mp4parser.boxes.adobe;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.mp4parser.boxes.iso14496.part12.FreeBox;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by sannies on 22.05.13.
 */
public class ActionMessageFormat0SampleEntryBoxTest extends BoxWriteReadBase<ActionMessageFormat0SampleEntryBox> {
    @Override
    public Class<ActionMessageFormat0SampleEntryBox> getBoxUnderTest() {
        return ActionMessageFormat0SampleEntryBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, ActionMessageFormat0SampleEntryBox box) {
        addPropsHere.put("boxes", (List) Collections.singletonList(new FreeBox(100)));
        addPropsHere.put("dataReferenceIndex", 4344);
    }
}
