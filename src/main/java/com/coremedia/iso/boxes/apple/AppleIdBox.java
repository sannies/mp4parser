package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleIdBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "apID";


    public AppleIdBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getStringAppleDataBox();
    }

    public String getDisplayName() {
        return "iTunes Apple Id Box";
    }


}