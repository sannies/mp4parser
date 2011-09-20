/* Copyright */
package com.googlecode.mp4parser.h264.model;

import java.io.IOException;
import java.io.OutputStream;

public abstract class BitstreamElement {

    public abstract void write(OutputStream out) throws IOException;
}
