package com.coremedia.iso.boxes.rtp;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * The smallest relative transmission time, in milliseconds.
 */
public class SmallestRelativeTransmissionTimeBox extends Box {
  public static final String TYPE = "tmin";

  long time;

  public SmallestRelativeTransmissionTimeBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  protected long getContentSize() {
    return 4;
  }

  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    time = in.readUInt32();
  }

  public String getDisplayName() {
    return "Smallest Relative Transmission Time";
  }

  protected void getContent(IsoOutputStream os) throws IOException {
    os.writeUInt32(time);
  }

  public long getTime() {
    return time;
  }

  public void setTime(long time) {
    this.time = time;
  }
}
