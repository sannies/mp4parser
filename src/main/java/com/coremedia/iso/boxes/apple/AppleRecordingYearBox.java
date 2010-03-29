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


  public void setRecordingYear(String recordingYear) {
    appleDataBox = new AppleDataBox();
    appleDataBox.setVersion(0);
    appleDataBox.setFlags(0);
    appleDataBox.setFourBytes(new byte[4]);
    appleDataBox.setContent(recordingYear.getBytes());
  }

  public String getRecordingYear() {
    return Utf8.convert(appleDataBox.getContent());
  }
}