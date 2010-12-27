package com.coremedia.iso.boxes.rtp;

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;

import java.io.IOException;

/**
 * The maximum data rate. This atom contains two numbers:<br/>
 * <p/>
 * g, followed by m (both 32-bit values).
 * <ul>
 * <li>g is the granularity, in milliseconds.</li>
 * <li>m is the maximum data rate given that granularity.</li>
 * </ul>
 * <br/>
 * For example, if g is 1 second, then m is the maximum data rate over any
 * 1 second. There may be multiple 'maxr' atoms, with different values for
 * g. The maximum data rate calculation does not include any network headers
 * (but does include 12-byte RTP headers).
 */
public class MaximumDataRateBox extends Box {
  public static final String TYPE = "maxr";

  public MaximumDataRateBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  private long g, m;

  public long getG() {
    return g;
  }

  public void setG(long g) {
    this.g = g;
  }

  public long getM() {
    return m;
  }

  public void setM(long m) {
    this.m = m;
  }

  protected long getContentSize() {
    return 8;
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    g = in.readUInt32();
    m = in.readUInt32();
  }

  public String getDisplayName() {
    return "Maximum Data Rate";
  }

  protected void getContent(IsoOutputStream os) throws IOException {
    os.writeUInt32(g);
    os.writeUInt32(m);
  }
}
