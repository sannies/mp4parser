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
 * Contained a the <code>UserDataBox</code> and containing information about the media's rating. E.g.
 * PG13or FSK16.
 */
public class RatingBox extends FullBox {
  public static final String TYPE = "rtng";

  private String ratingEntity;
  private String ratingCriteria;
  private String language;
  private String ratingInfo;

  public RatingBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }


  public void setRatingEntity(String ratingEntity) {
    this.ratingEntity = ratingEntity;
  }

  public void setRatingCriteria(String ratingCriteria) {
    this.ratingCriteria = ratingCriteria;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setRatingInfo(String ratingInfo) {
    this.ratingInfo = ratingInfo;
  }

  public String getLanguage() {
    return language;
  }

  /**
   * Gets a four-character code that indicates the rating entity grading the asset, e.g., 'BBFC'. The values of this
   * field should follow common names of worldwide movie rating systems, such as those mentioned in
   * [http://www.movie-ratings.net/, October 2002].
   *
   * @return the rating organization
   */
  public String getRatingEntity() {
    return ratingEntity;
  }

  /**
   * Gets the four-character code that indicates which rating criteria are being used for the corresponding rating
   * entity, e.g., 'PG13'.
   *
   * @return the actual rating
   */
  public String getRatingCriteria() {
    return ratingCriteria;
  }

  public String getRatingInfo() {
    return ratingInfo;
  }

  public String getDisplayName() {
    return "Rating Box";
  }

  protected long getContentSize() {
    return 4 + 4 + 2 + utf8StringLengthInBytes(ratingInfo) + 1;
  }

  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    ratingEntity = IsoFile.bytesToFourCC(in.read(4));
    ratingCriteria = IsoFile.bytesToFourCC(in.read(4));
    language = in.readIso639();
    ratingInfo = in.readString();
  }

  protected void getContent(IsoOutputStream isos) throws IOException {
    isos.write(IsoFile.fourCCtoBytes(ratingEntity));
    isos.write(IsoFile.fourCCtoBytes(ratingCriteria));
    isos.writeIso639(language);
    isos.writeStringZeroTerm(ratingInfo);
  }

  public String toString() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("RatingBox[language=").append(getLanguage());
    buffer.append("ratingEntity=").append(getRatingEntity());
    buffer.append(";ratingCriteria=").append(getRatingCriteria());
    buffer.append(";language=").append(getLanguage());
    buffer.append(";ratingInfo=").append(getRatingInfo());
    buffer.append("]");
    return buffer.toString();
  }
}
