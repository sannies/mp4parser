package com.coremedia.iso.boxes.apple;

import com.googlecode.mp4parser.AbstractContainerBox;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public final class AppleGenericBox extends AbstractContainerBox {
    public static final String TYPE = "----";

    public AppleGenericBox() {
        super(TYPE);
    }

}