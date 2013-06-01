package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * Tv Episode.
 */
public class AppleTvEpisodeNumberBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "tven";


    public AppleTvEpisodeNumberBox() {
        super(TYPE);
    }

}