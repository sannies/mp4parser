package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public final class AppleIdBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "apID";


    public AppleIdBox() {
        super(TYPE);
    }

}