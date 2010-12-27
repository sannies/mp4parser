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

package com.coremedia.iso.boxes.vodafone;


import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FullBox;

import java.io.IOException;

/**
 * Special box used by Vodafone in their DCF containing information about the artist. Mainly used for OMA DCF files
 * containing music. Resides in the {@link com.coremedia.iso.boxes.UserDataBox}.
 */
public class AlbumArtistBox extends FullBox {
  public static final String TYPE = "albr";

  private String language;
  private String albumArtist;

  public AlbumArtistBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getLanguage() {
    return language;
  }

  public String getAlbumArtist() {
    return albumArtist;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setAlbumArtist(String albumArtist) {
    this.albumArtist = albumArtist;
  }

  public String getDisplayName() {
    return "Album Artist Box";
  }

  protected long getContentSize() {
    return 2 + utf8StringLengthInBytes(albumArtist) + 1;
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    language = in.readIso639();
    albumArtist = in.readString();
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.writeIso639(language);
    isos.writeStringZeroTerm(albumArtist);
  }

  public String toString() {
    return "AlbumArtistBox[language=" + getLanguage() + ";albumArtist=" + getAlbumArtist() + "]";
  }
}
