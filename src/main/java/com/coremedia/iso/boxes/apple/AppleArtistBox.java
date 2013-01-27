package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * iTunes Artist box.
 */
public final class AppleArtistBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "\u00a9ART";


    public AppleArtistBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getStringAppleDataBox();
    }


}
