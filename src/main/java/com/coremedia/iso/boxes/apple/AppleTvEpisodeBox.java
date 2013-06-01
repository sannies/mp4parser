package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * Tv Episode.
 */
public class AppleTvEpisodeBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "tves";


    public AppleTvEpisodeBox() {
        super(TYPE);
    }

}