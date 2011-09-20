/* Copyright */
package com.googlecode.mp4parser.h264.model;

public class SliceType {
    private String type;
    private int value;

    public static SliceType P = new SliceType("P", 0);
    public static SliceType B = new SliceType("B", 1);
    public static SliceType I = new SliceType("I", 2);
    public static SliceType SP = new SliceType("SP", 3);
    public static SliceType SI = new SliceType("SI", 4);

    private SliceType(String type, int value) {
        this.type = type;
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static SliceType fromValue(int real) {
        if (real == P.value) {
            return P;
        } else if (real == B.value) {
            return B;
        } else if (real == I.value) {
            return I;
        } else if (real == SP.value) {
            return SP;
        } else if (real == SI.value) {
            return SI;
        }
        return null;
    }
}
