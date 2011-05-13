package com.coremedia.iso.boxes.sampleentry;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MpegSampleEntry extends SampleEntry implements ContainerBox {
  
  public MpegSampleEntry(byte[] type) {
    super(type);
  }

  @Override
  public void parse(IsoBufferWrapper in, long size, BoxParser boxParser, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxParser, lastMovieFragmentBox);

    while (size > 8) {
      Box b = boxParser.parseBox(in, this, lastMovieFragmentBox);
      boxes.add(b);
      size -= b.getSize();
    }
  }



  @Override
  protected long getContentSize() {
    long contentSize = 8;
    for (Box boxe : boxes) {
      contentSize += boxe.getSize();
    }
    return contentSize;
  }

  @Override
  public String getDisplayName() {
    return "Mpeg Sample Entry";
  }

  public String toString() {
    return "MpegSampleEntry" + Arrays.asList(getBoxes());
  }

  @Override
  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.write(new byte[6]);
    isos.writeUInt16(getDataReferenceIndex());

    for (Box boxe : boxes) {
      boxe.getBox(isos);
    }
  }

}
