package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.Utf8;

/**
 *
 */
public class AppleRecordingYearBox extends AbstractAppleMetaDataBox {
  public static final String TYPE = "\u00a9day";


  public AppleRecordingYearBox() {
    super(TYPE);
  }

  public String getDisplayName() {
    return "iTunes Recording Year";
  }


}