package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 * itunes MetaData comment box.
 */
public final class AppleCommentBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9cmt";


  public AppleCommentBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Comment";
  }


}
