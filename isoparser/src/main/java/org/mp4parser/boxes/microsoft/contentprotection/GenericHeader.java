package org.mp4parser.boxes.microsoft.contentprotection;

import org.mp4parser.boxes.microsoft.ProtectionSpecificHeader;

import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Specifications &gt; Microsoft PlayReady Format Specification &gt; 2. PlayReady Media Format &gt; 2.7. ASF GUIDs
 * <p>
 * ASF_Protection_System_Identifier_Object
 * 9A04F079-9840-4286-AB92E65BE0885F95</p>
 * <p>
 * ASF_Content_Protection_System_Microsoft_PlayReady
 * F4637010-03C3-42CD-B932B48ADF3A6A54    </p>
 * <p>
 * ASF_StreamType_PlayReady_Encrypted_Command_Media
 * 8683973A-6639-463A-ABD764F1CE3EEAE0</p>
 * <p>
 * Specifications &gt; Microsoft PlayReady Format Specification &gt; 2. PlayReady Media Format &gt; 2.5. Data Objects &gt; 2.5.1. Payload TrackExtension for AES in Counter Mode</p>
 * <p>
 * The sample Id is used as the IV in CTR mode. Block offset, starting at 0 and incremented by 1 after every 16 bytes, from the beginning of the sample is used as the Counter.</p>
 * <p>
 * The sample ID for each sample (media object) is stored as an ASF payload extension system with the ID of ASF_Payload_Extension_Encryption_SampleID = {6698B84E-0AFA-4330-AEB2-1C0A98D7A44D}. The payload extension can be stored as a fixed size extension of 8 bytes.</p>
 * <p>
 * The sample ID is always stored in big-endian byte order.</p>
 */
public class GenericHeader extends ProtectionSpecificHeader {
    public static UUID PROTECTION_SYSTEM_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");

    static {
        uuidRegistry.put(PROTECTION_SYSTEM_ID, GenericHeader.class);
    }

    ByteBuffer data;

    @Override
    public UUID getSystemId() {
        return PROTECTION_SYSTEM_ID;
    }

    @Override
    public void parse(ByteBuffer buffer) {
        data = buffer;
    }

    @Override
    public ByteBuffer getData() {
        return data;
    }

}
