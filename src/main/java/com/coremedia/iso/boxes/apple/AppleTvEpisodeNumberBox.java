package com.coremedia.iso.boxes.apple;

/**
 * Tv Episode.
 */
public class AppleTvEpisodeNumberBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "tven";


    public AppleTvEpisodeNumberBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getStringAppleDataBox();
    }

    public String getDisplayName() {
        return "iTunes TV Episode Number Box";
    }
}