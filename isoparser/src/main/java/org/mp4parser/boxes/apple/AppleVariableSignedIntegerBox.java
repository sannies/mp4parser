package org.mp4parser.boxes.apple;

import org.mp4parser.tools.IsoTypeReaderVariable;
import org.mp4parser.tools.IsoTypeWriterVariable;

import java.nio.ByteBuffer;

/**
 * Created by sannies on 10/22/13.
 */
public abstract class AppleVariableSignedIntegerBox extends AppleDataBox {
    long value;
    int intLength = 1;

    protected AppleVariableSignedIntegerBox(String type) {
        super(type, 15);
    }

    public int getIntLength() {
        return intLength;
    }

    public void setIntLength(int intLength) {
        this.intLength = intLength;
    }

    public long getValue() {
        //patched by Tobias Bley / UltraMixer (04/25/2014)
        if (!isParsed()) {
            parseDetails();
        }
        return value;
    }

    public void setValue(long value) {

        if (value <= 127 && value > -128) {
            intLength = 1;
        } else if (value <= 32767 && value > -32768 && intLength < 2) {
            intLength = 2;
        } else if (value <= 8388607 && value > -8388608 && intLength < 3) {
            intLength = 3;
        } else {
            intLength = 4;
        }

        this.value = value;
    }

    @Override
    protected byte[] writeData() {
        int dLength = getDataLength();
        ByteBuffer b = ByteBuffer.wrap(new byte[dLength]);
        IsoTypeWriterVariable.write(value, b, dLength);
        return b.array();
    }

    @Override
    protected void parseData(ByteBuffer data) {
        int intLength = data.remaining();
        value = IsoTypeReaderVariable.read(data, intLength);
        this.intLength = intLength;
    }

    @Override
    protected int getDataLength() {
        return intLength;
    }
}
