/*
Copyright (c) 2011 Stanislav Vitvitskiy

Permission is hereby granted, free of charge, to any person obtaining a copy of this
software and associated documentation files (the "Software"), to deal in the Software
without restriction, including without limitation the rights to use, copy, modify,
merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
permit persons to whom the Software is furnished to do so, subject to the following
conditions:

The above copyright notice and this permission notice shall be included in all copies or
substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
OR OTHER DEALINGS IN THE SOFTWARE.
*/
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

    @Override
    public String toString() {
        return "SliceType{" +
                "type='" + type + '\'' +
                ", value=" + value +
                '}';
    }
}
