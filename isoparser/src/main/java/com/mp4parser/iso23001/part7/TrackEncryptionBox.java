package com.mp4parser.iso23001.part7;

import com.googlecode.mp4parser.boxes.AbstractTrackEncryptionBox;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public class TrackEncryptionBox extends AbstractTrackEncryptionBox {
    public static final String TYPE = "tenc";

    public TrackEncryptionBox() {
        super(TYPE);
    }
}
