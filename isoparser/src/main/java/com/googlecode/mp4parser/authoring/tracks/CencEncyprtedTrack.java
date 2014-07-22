package com.googlecode.mp4parser.authoring.tracks;

import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.boxes.cenc.CencSampleAuxiliaryDataFormat;

import java.util.List;
import java.util.UUID;

/**
 * Track encrypted with common (CENC). ISO/IEC 23001-7.
 */
public interface CencEncyprtedTrack extends Track {
    List<CencSampleAuxiliaryDataFormat> getSampleEncryptionEntries();
    UUID getKeyId();
    boolean hasSubSampleEncryption();
}
