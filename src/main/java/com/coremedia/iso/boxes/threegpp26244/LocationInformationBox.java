package com.coremedia.iso.boxes.threegpp26244;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.coremedia.iso.boxes.AbstractFullBox;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Location Information Box as specified in TS 26.244.
 */
public class LocationInformationBox extends AbstractFullBox {
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
        super(TYPE);
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

    protected long getContentSize() {
        return 22 + Utf8.convert(name).length + Utf8.convert(astronomicalBody).length + Utf8.convert(additionalNotes).length;
    }

    @Override
    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        language = IsoTypeReader.readIso639(content);
        name = IsoTypeReader.readString(content);
        role = IsoTypeReader.readUInt8(content);
        longitude = IsoTypeReader.readFixedPoint1616(content);
        latitude = IsoTypeReader.readFixedPoint1616(content);
        altitude = IsoTypeReader.readFixedPoint1616(content);
        astronomicalBody = IsoTypeReader.readString(content);
        additionalNotes = IsoTypeReader.readString(content);
    }


    @Override
    protected void getContent(ByteBuffer bb) throws IOException {
        writeVersionAndFlags(bb);
        IsoTypeWriter.writeIso639(bb, language);
        bb.put(Utf8.convert(name));
        bb.put((byte) 0);
        IsoTypeWriter.writeUInt8(bb, role);
        IsoTypeWriter.writeFixedPont1616(bb, longitude);
        IsoTypeWriter.writeFixedPont1616(bb, latitude);
        IsoTypeWriter.writeFixedPont1616(bb, altitude);
        bb.put(Utf8.convert(astronomicalBody));
        bb.put((byte) 0);
        bb.put(Utf8.convert(additionalNotes));
        bb.put((byte) 0);
    }
}
