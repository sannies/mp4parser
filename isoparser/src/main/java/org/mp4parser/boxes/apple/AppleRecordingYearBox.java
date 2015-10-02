package org.mp4parser.boxes.apple;

import org.mp4parser.tools.IsoTypeReader;
import org.mp4parser.tools.Utf8;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by sannies on 10/22/13.
 */
public class AppleRecordingYearBox extends AppleDataBox {
    DateFormat df;

    Date date = new Date();

    public AppleRecordingYearBox() {
        super("©day", 1);
        df = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ssZ");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    protected static String iso8601toRfc822Date(String iso8601) {
        iso8601 = iso8601.replaceAll("Z$", "+0000");
        iso8601 = iso8601.replaceAll("([0-9][0-9]):([0-9][0-9])$", "$1$2");
        return iso8601;
    }

    protected static String rfc822toIso8601Date(String rfc622) {
        rfc622 = rfc622.replaceAll("\\+0000$", "Z");
        return rfc622;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    protected byte[] writeData() {

        return Utf8.convert(rfc822toIso8601Date(df.format(date)));
    }

    @Override
    protected void parseData(ByteBuffer data) {
        String dateString = IsoTypeReader.readString(data, data.remaining());
        try {
            date = df.parse(iso8601toRfc822Date(dateString));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int getDataLength() {
        return Utf8.convert(rfc822toIso8601Date(df.format(date))).length;
    }
}
