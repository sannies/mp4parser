package com.mp4parser.tools.boxes;

import com.googlecode.mp4parser.boxes.BoxWriteReadBase;
import com.mp4parser.boxes.iso14496.part12.EditListBox;

import java.util.Arrays;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sannies
 * Date: 11/18/12
 * Time: 10:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class EditListBoxTest extends BoxWriteReadBase<EditListBox> {
    @Override
    public Class<EditListBox> getBoxUnderTest() {
        return EditListBox.class;
    }

    @Override
    public void setupProperties(Map<String, Object> addPropsHere, EditListBox box) {
        addPropsHere.put("entries", Arrays.asList(new EditListBox.Entry(box, 12423, 0, 1), new EditListBox.Entry(box, 12423, 0, 0.5)));

    }
}
