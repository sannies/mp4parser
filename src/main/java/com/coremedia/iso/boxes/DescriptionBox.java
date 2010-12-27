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

package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoOutputStream;

import java.io.IOException;

/**
 * Gives a language dependent description of the media contained in the ISO file.
 */
public class DescriptionBox extends FullBox {
  public static final String TYPE = "dscp";

  private String language;
  private String description;

  public DescriptionBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getLanguage() {
    return language;
  }

  public String getDescription() {
    return description;
  }

  public String getDisplayName() {
    return "Description Box";
  }

  protected long getContentSize() {
    return 2 + utf8StringLengthInBytes(description) + 1;
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    language = in.readIso639();
    description = in.readString();
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.writeIso639(language);
    isos.writeStringZeroTerm(description);
  }


  public String toString() {
    return "DescriptionBox[language=" + getLanguage() + ";description=" + getDescription() + "]";
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setDescription(String description) {
    this.description = description;
  }
}
