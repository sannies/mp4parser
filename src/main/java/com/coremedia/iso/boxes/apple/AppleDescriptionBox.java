package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public final class AppleDescriptionBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "desc";


    public AppleDescriptionBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getStringAppleDataBox();
    }

}