package com.googlecode.mp4parser.boxes.apple;

import java.nio.ByteBuffer;

/**
 * Created by Tobias Bley / UltraMixer on 04/25/2014.
 *
 * 2014-07-22 @aldenml Added minimal support for image data manipulation (read and write).
 */
public class AppleCoverBox extends AppleDataBox {
    
    private static final int IMAGE_TYPE_JPG = 13;
    private static final int IMAGE_TYPE_PNG = 14;
    
    private byte[] data;
    
    public AppleCoverBox() {
        super("covr", 1);
    }
    
    public byte[] getCoverData() {
        return data;
    }
    
    public void setJpg(byte[] data) {
        setImageData(data, IMAGE_TYPE_JPG);
    }
    
    public void setPng(byte[] data) {
        setImageData(data, IMAGE_TYPE_PNG);
    }
    
    @Override
    protected byte[] writeData() {
        return data;
    }
    
    @Override
    protected void parseData(ByteBuffer data) {
        this.data = new byte[data.limit()];
        data.get(this.data);
    }
    
    @Override
    protected int getDataLength() {
        return data.length;
    }
    
    private void setImageData(byte[] data, int dataType) {
        this.data = data;
        this.dataType = dataType;
    }
}
