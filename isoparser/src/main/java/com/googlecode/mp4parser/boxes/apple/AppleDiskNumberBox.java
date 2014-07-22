package com.googlecode.mp4parser.boxes.apple;

import java.nio.ByteBuffer;

/**
 * Created by sannies on 10/15/13.
 */
public class AppleDiskNumberBox extends AppleDataBox {
    public AppleDiskNumberBox() {
        super("disk", 0);
    }

    int a;
    short b;

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public short getB() {
        return b;
    }

    public void setB(short b) {
        this.b = b;
    }

    @Override
    protected byte[] writeData() {
        ByteBuffer bb = ByteBuffer.allocate(6);
        bb.putInt(a);
        bb.putShort(b);
        return bb.array();
    }

    @Override
    protected void parseData(ByteBuffer data) {
        a = data.getInt();
        b = data.getShort();
    }

    @Override
    protected int getDataLength() {
        return 6;
    }
}
