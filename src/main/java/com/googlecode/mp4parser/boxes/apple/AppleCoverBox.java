package com.googlecode.mp4parser.boxes.apple;

import java.nio.ByteBuffer;
import java.util.logging.Logger;

/**
 * Created by Tobias Bley / UltraMixer on 04/25/2014.
 */
public class AppleCoverBox extends AppleDataBox {
    private Logger logger = Logger.getLogger(getClass().getName());
    private byte[] data;

    public AppleCoverBox() {
        super("covr", 1);
    }

    @Override
    protected byte[] writeData() {
        return data;
    }

    @Override
    protected void parseData(ByteBuffer data) {
        this.data = data.array();
    }

    @Override
    protected int getDataLength() {
        return data.length;
    }

}
