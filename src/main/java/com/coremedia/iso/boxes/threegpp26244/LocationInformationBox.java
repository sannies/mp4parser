package com.coremedia.iso.boxes.threegpp26244;

import com.coremedia.iso.BoxFactory;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoBufferWrapper;
import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.Utf8;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.FullBox;

import java.io.IOException;

/**
 * Location Information Box as specified in TS 26.244.
 */
public class LocationInformationBox extends FullBox {
  public static final String TYPE = "loci";

  private String language;
  private String name = "";
  private int role;
  private double longitude;
  private double latitude;
  private double altitude;
  private String astronomicalBody = "";
  private String additionalNotes = "";

  public LocationInformationBox() {
    super(IsoFile.fourCCtoBytes(TYPE));
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getRole() {
    return role;
  }

  public void setRole(int role) {
    this.role = role;
  }

  public double getLongitude() {
    return longitude;
  }

  public void setLongitude(double longitude) {
    this.longitude = longitude;
  }

  public double getLatitude() {
    return latitude;
  }

  public void setLatitude(double latitude) {
    this.latitude = latitude;
  }

  public double getAltitude() {
    return altitude;
  }

  public void setAltitude(double altitude) {
    this.altitude = altitude;
  }

  public String getAstronomicalBody() {
    return astronomicalBody;
  }

  public void setAstronomicalBody(String astronomicalBody) {
    this.astronomicalBody = astronomicalBody;
  }

  public String getAdditionalNotes() {
    return additionalNotes;
  }

  public void setAdditionalNotes(String additionalNotes) {
    this.additionalNotes = additionalNotes;
  }

  public String getDisplayName() {
    return "Location Information Box";
  }

  protected long getContentSize() {
    return 18 + Utf8.convert(name).length + Utf8.convert(astronomicalBody).length + Utf8.convert(additionalNotes).length;
  }

  @Override
  public void parse(IsoBufferWrapper in, long size, BoxFactory boxFactory, Box lastMovieFragmentBox) throws IOException {
    super.parse(in, size, boxFactory, lastMovieFragmentBox);
    language = in.readIso639();
    name = in.readString();
    role = in.readUInt8();
    longitude = in.readFixedPoint1616();
    latitude = in.readFixedPoint1616();
    altitude = in.readFixedPoint1616();
    astronomicalBody = in.readString();
    additionalNotes = in.readString();
  }

  protected void getContent(IsoOutputStream os) throws IOException {
    os.writeIso639(language);
    os.writeStringZeroTerm(name);
    os.writeUInt8(role);
    os.writeFixedPont1616(longitude);
    os.writeFixedPont1616(latitude);
    os.writeFixedPont1616(altitude);
    os.writeStringZeroTerm(astronomicalBody);
    os.writeStringZeroTerm(additionalNotes);
  }
}
