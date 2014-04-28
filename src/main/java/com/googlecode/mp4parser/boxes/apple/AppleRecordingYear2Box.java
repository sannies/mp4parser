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
 * Created by Tobias Bley, / UltraMixer
 */
public class AppleRecordingYear2Box extends Utf8AppleDataBox {

    public AppleRecordingYear2Box() {
        super("©day");
    }

}
