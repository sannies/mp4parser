package org.mp4parser.test.boxes.sampleentry;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.mp4parser.boxes.sampleentry.VisualSampleEntry;

import java.util.Map;

/**
 * Created by sannies on 23.05.13.
 */
public class VisualSampleEntryTest extends BoxWriteReadBase<VisualSampleEntry> {
    @Override
    public Class<VisualSampleEntry> getBoxUnderTest() {
        return VisualSampleEntry.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, VisualSampleEntry box) {

    }
}
