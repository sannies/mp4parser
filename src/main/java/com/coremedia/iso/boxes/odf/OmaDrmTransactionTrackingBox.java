/*  
 * Copyright 2008 CoreMedia AG, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an AS IS BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */

package com.coremedia.iso.boxes.odf;


import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FullBox;

import java.io.IOException;

/**
 * The OMA DRM Transaction Tracking Box enables transaction tracking as defined OMA DRM 2.0. Resides in a
 * {@link com.coremedia.iso.boxes.odf.MutableDrmInformationBox}.
 */
public class OmaDrmTransactionTrackingBox extends FullBox {
  public static final String TYPE = "odtt";

  private byte[] transactionId = new byte[16];

  public OmaDrmTransactionTrackingBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public void setTransactionId(byte[] transactionId) {
    assert transactionId.length == 16;
    this.transactionId = transactionId;
  }

  public byte[] getTransactionId() {
    return transactionId;
  }

  public String getDisplayName() {
    return "OMA DRM Tranaction Tracking Box";
  }

  protected long getContentSize() {
    return 16;
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    int a = in.read(transactionId);
    assert a == 16;
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.write(transactionId);
  }

  public String toString() {
    return "OmaDrmTransactionTrackingBox[transactionId=" + getTransactionId() + "]";
  }
}
