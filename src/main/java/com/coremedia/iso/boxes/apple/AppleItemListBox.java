package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.ContainerBox;

/**
 * undocumented iTunes MetaData Box.
 */
public class AppleItemListBox extends ContainerBox {
  public static final String TYPE = "ilst";

  public AppleItemListBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getDisplayName() {
    return "iTunes Meta Data";
  }
}
