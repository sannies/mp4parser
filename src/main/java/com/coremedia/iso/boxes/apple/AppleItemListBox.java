package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.AbstractContainerBox;

/**
 * undocumented iTunes MetaData Box.
 */
public class AppleItemListBox extends AbstractContainerBox {
    public static final String TYPE = "ilst";

    public AppleItemListBox() {
        super(IsoFile.fourCCtoBytes(TYPE));
    }

}
