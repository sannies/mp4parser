package com.mp4parser.boxes.apple;

import com.mp4parser.support.AbstractContainerBox;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * undocumented iTunes MetaData Box.
 */
public class AppleItemListBox extends AbstractContainerBox {
    public static final String TYPE = "ilst";

    public AppleItemListBox() {
        super(TYPE);
    }

}
