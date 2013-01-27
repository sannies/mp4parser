package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public final class AppleSortAlbumBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "soal";


    public AppleSortAlbumBox() {
        super(TYPE);
        appleDataBox = AppleDataBox.getStringAppleDataBox();
    }
}