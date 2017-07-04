package org.mp4parser.muxer.tracks.encryption;

import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.muxer.Track;

import java.util.List;
import java.util.UUID;

/**
 * Track encrypted with common (CENC). ISO/IEC 23001-7.
 */
public interface CencEncryptedTrack extends Track {
    List<CencSampleAuxiliaryDataFormat> getSampleEncryptionEntries();

    boolean hasSubSampleEncryption();
}
