package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FullBox;

import java.io.IOException;

/**
 * Most stupid box of the world. Encapsulates actual data within
 */
public final class AppleDataBox extends FullBox {
  public static final String TYPE = "data";

  private byte[] fourBytes = new byte[4];
  private byte[] content;


  public AppleDataBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  protected long getContentSize() {
    return content.length + 4;
  }

  public String getDisplayName() {
    return "iTunes Data Box";
  }

  public void setContent(byte[] content) {
    this.content = new byte[content.length];
    System.arraycopy(content, 0, this.content, 0, content.length);
  }

  public void setFourBytes(byte[] fourBytes) {
    System.arraycopy(fourBytes, 0, this.fourBytes, 0, 4);
  }

  @Override
  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    fourBytes[0] = (byte) in.read();
    fourBytes[1] = (byte) in.read();
    fourBytes[2] = (byte) in.read();
    fourBytes[3] = (byte) in.read();
    size -= (4 + 4);
    content = in.read((int) size);

  }

  protected void getContent(IsoOutputStream os) throws IOException {
    os.write(fourBytes, 0, 4);
    os.write(content);
  }

  public byte[] getFourBytes() {
    return fourBytes;
  }

  public byte[] getContent() {
    return content;
  }
}
