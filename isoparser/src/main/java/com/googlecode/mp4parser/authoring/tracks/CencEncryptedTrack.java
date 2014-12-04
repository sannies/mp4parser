package com.googlecode.mp4parser.authoring.tracks;

import com.googlecode.mp4parser.authoring.Track;
import com.mp4parser.iso23001.part7.CencSampleAuxiliaryDataFormat;

import java.util.List;
import java.util.UUID;

/**
 * Track encrypted with common (CENC). ISO/IEC 23001-7.
 */
public interface CencEncryptedTrack extends Track {
    List<CencSampleAuxiliaryDataFormat> getSampleEncryptionEntries();

    UUID getDefaultKeyId();
    boolean hasSubSampleEncryption();
}
