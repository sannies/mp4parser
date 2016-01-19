package org.mp4parser.tools.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.mp4parser.boxes.iso14496.part12.ProgressiveDownloadInformationBox;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class ProgressiveDownloadInformationBoxTest extends BoxWriteReadBase<ProgressiveDownloadInformationBox> {
    @Override
    public Class<ProgressiveDownloadInformationBox> getBoxUnderTest() {
        return ProgressiveDownloadInformationBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, ProgressiveDownloadInformationBox box) {
        List<ProgressiveDownloadInformationBox.Entry> entries = new LinkedList<ProgressiveDownloadInformationBox.Entry>();
        entries.add(new ProgressiveDownloadInformationBox.Entry(10, 20));
        entries.add(new ProgressiveDownloadInformationBox.Entry(20, 10));
        addPropsHere.put("entries", entries);
    }
}
