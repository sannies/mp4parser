package com.coremedia.iso.boxes.apple;

/**
 *
 */
public final class AppleStandardGenreBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "gnre";


    public AppleStandardGenreBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getUint16AppleDataBox();
    }

    public String getDisplayName() {
        return "iTunes Standard Genre";
    }


}