package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public final class AppleAlbumBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "\u00a9alb";


    public AppleAlbumBox() {
        super(TYPE);
    }

}