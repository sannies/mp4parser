package com.mp4parser.boxes.iso23001.part7;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 */
public class TrackEncryptionBox extends AbstractTrackEncryptionBox {
    public static final String TYPE = "tenc";

    public TrackEncryptionBox() {
        super(TYPE);
    }
}
