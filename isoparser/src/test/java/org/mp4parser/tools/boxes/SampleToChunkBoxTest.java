package org.mp4parser.tools.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import org.mp4parser.boxes.iso14496.part12.SampleToChunkBox;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 24.02.11
 * Time: 12:41
 * To change this template use File | Settings | File Templates.
 */
public class SampleToChunkBoxTest extends BoxWriteReadBase<SampleToChunkBox> {


    @Override
    public Class<SampleToChunkBox> getBoxUnderTest() {
        return SampleToChunkBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, SampleToChunkBox box) {
        List<SampleToChunkBox.Entry> l = new LinkedList<SampleToChunkBox.Entry>();
        for (int i = 0; i < 5; i++) {
            SampleToChunkBox.Entry e = new SampleToChunkBox.Entry(i, 1, i * i);
            l.add(e);
        }

        addPropsHere.put("entries", l);
    }
}
