package org.mp4parser.boxes.apple;

import java.nio.ByteBuffer;

/**
 * Created by sannies on 10/15/13.
 */
public class AppleTrackNumberBox extends AppleDataBox {
    int a;
    int b;

    public AppleTrackNumberBox() {
        super("trkn", 0);
    }

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public int getB() {
        return b;
    }

    public void setB(int b) {
        this.b = b;
    }

    @Override
    protected byte[] writeData() {
        ByteBuffer bb = ByteBuffer.allocate(8);
        bb.putInt(a);
        bb.putInt(b);
        return bb.array();
    }

    @Override
    protected void parseData(ByteBuffer data) {
        a = data.getInt();
        b = data.getInt();
    }

    @Override
    protected int getDataLength() {
        return 8;
    }
}
