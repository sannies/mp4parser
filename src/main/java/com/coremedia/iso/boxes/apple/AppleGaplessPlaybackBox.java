package com.coremedia.iso.boxes.apple;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * Gapless Playback.
 */
public final class AppleGaplessPlaybackBox extends AbstractAppleMetaDataBox {
    public static final String TYPE = "pgap";


    public AppleGaplessPlaybackBox() {
        super(TYPE);
    }

}
