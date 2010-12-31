package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleDescriptionBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "desc";


    public AppleDescriptionBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getStringAppleDataBox();
    }

    public String getDisplayName() {
        return "iTunes Description Box";
    }

}