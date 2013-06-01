package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public final class ApplePurchaseDateBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "purd";


    public ApplePurchaseDateBox() {
        super(TYPE);
    }

}