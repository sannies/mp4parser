package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxContainer;

import java.io.IOException;
import java.lang.reflect.Array;

/**
 *
 */
public abstract class AbstractAppleMetaDataBox extends Box implements BoxContainer {
  AppleDataBox appleDataBox = new AppleDataBox();

  public Box[] getBoxes() {
    return new Box[]{appleDataBox};
  }

  public <T extends Box> T[] getBoxes(Class<T> clazz) {
    if (appleDataBox.getClass().isInstance(clazz)) {
      T[] returnValue = (T[]) Array.newInstance(clazz, 1);
      returnValue[0] = (T) appleDataBox;
      return returnValue;
    }
    return null;
  }

  public AbstractAppleMetaDataBox(String type) {
    super(IsoFile.fourCCtoBytes(type));
  }


  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    long sp = in.position();
    long dataBoxSize = in.readUInt32();
    String thisShouldBeData = in.readString(4);
    assert "data".equals(thisShouldBeData);
    appleDataBox = new AppleDataBox();
    appleDataBox.parse(in, dataBoxSize - 8, boxFactory, lastMovieFragmentBox);
    appleDataBox.setParent(this);
    appleDataBox.offset = sp;
  }


  protected long getContentSize() {
    return appleDataBox.getSize();
  }

  protected void getContent(IsoOutputStream os) throws IOException {
    appleDataBox.getBox(os);
  }

  public long getNumOfBytesToFirstChild() {
    return getSize() - appleDataBox.getSize();
  }

  @Override
  public String toString() {
    return this.getClass().getSimpleName() + "[" +
            "appleDataBox=" + appleDataBox +
            '}';
  }
}
