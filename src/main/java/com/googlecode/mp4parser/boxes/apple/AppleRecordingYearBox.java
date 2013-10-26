package com.googlecode.mp4parser.boxes.apple;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.Utf8;

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
        df  = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ssX");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    protected byte[] writeData() {

        return Utf8.convert(df.format(date));
    }

    @Override
    protected void parseData(ByteBuffer data) {
        String dateString = IsoTypeReader.readString(data, data.remaining());
        try {
            date = df.parse(dateString);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected int getDataLength() {
        return Utf8.convert(df.format(date)).length;
    }
}
