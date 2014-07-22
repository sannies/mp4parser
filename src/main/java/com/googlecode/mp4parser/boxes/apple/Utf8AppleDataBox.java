package com.googlecode.mp4parser.boxes.apple;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.annotations.DoNotParseDetail;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

/**
 * 
 */
public abstract class Utf8AppleDataBox extends AppleDataBox {
    String value;

    protected Utf8AppleDataBox(String type) {
        super(type, 1);
    }

    public String getValue() {
        //patched by Toias Bley / UltraMixer
        if(!isParsed())
        {
            parseDetails();
        }
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @DoNotParseDetail
    public byte[] writeData() {
        return Utf8.convert(value);
    }

    @Override
    protected int getDataLength() {
        return value.getBytes(Charset.forName("UTF-8")).length;
    }

    @Override
    protected void parseData(ByteBuffer data) {
        value = IsoTypeReader.readString(data, data.remaining());
    }
}
