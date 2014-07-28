package com.googlecode.mp4parser.boxes.dece;

import com.googlecode.mp4parser.annotations.DoNotParseDetail;
import com.googlecode.mp4parser.boxes.AbstractSampleEncryptionBox;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * The Sample Encryption Box contains the sample specific encryption data, including the initialization
 * vectors needed for decryption and, optionally, alternative decryption parameters. It is used when the
 * sample data in the fragment might be encrypted.
 */
public class SampleEncryptionBox extends AbstractSampleEncryptionBox {
    public static final String TYPE = "senc";

    /**
     * Creates a SampleEncryptionBox for non-h264 tracks.
     */
    public SampleEncryptionBox() {
        super(TYPE);
    }


}
