package org.mp4parser.boxes.microsoft;

import org.mp4parser.boxes.iso23001.part7.AbstractSampleEncryptionBox;
import org.mp4parser.support.DoNotParseDetail;

/**
 * <pre>
 * aligned(8) class SampleEncryptionBox extends FullBox(‘uuid’, extended_type= 0xA2394F52-5A9B-4f14-A244-6C427C648DF4, version=0, flags=0)
 * {
 *  if (flags &amp; 0x000001)
 *  {
 *   unsigned int(24) AlgorithmID;
 *   unsigned int(8) IV_size;
 *   unsigned int(8)[16] KID;
 *  }
 *  unsigned int (32) sample_count;
 *  {
 *   unsigned int(IV_size) InitializationVector;
 *   if (flags &amp; 0x000002)
 *   {
 *    unsigned int(16) NumberOfEntries;
 *    {
 *     unsigned int(16) BytesOfClearData;
 *     unsigned int(32) BytesOfEncryptedData;
 *    } [ NumberOfEntries]
 *   }
 *  }[ sample_count ]
 * }
 * </pre>
 */
public class PiffSampleEncryptionBox extends AbstractSampleEncryptionBox {

    /**
     * Creates a AbstractSampleEncryptionBox for non-h264 tracks.
     */
    public PiffSampleEncryptionBox() {
        super("uuid");

    }

    @Override
    public byte[] getUserType() {
        return new byte[]{(byte) 0xA2, 0x39, 0x4F, 0x52, 0x5A, (byte) 0x9B, 0x4f, 0x14, (byte) 0xA2, 0x44, 0x6C, 0x42, 0x7C, 0x64, (byte) 0x8D, (byte) 0xF4};
    }

    public int getAlgorithmId() {
        return algorithmId;
    }

    public void setAlgorithmId(int algorithmId) {
        this.algorithmId = algorithmId;
    }

    public int getIvSize() {
        return ivSize;
    }

    public void setIvSize(int ivSize) {
        this.ivSize = ivSize;
    }

    public byte[] getKid() {
        return kid;
    }

    public void setKid(byte[] kid) {
        this.kid = kid;
    }

    @DoNotParseDetail
    public boolean isOverrideTrackEncryptionBoxParameters() {
        return (getFlags() & 0x1) > 0;
    }


    @DoNotParseDetail
    public void setOverrideTrackEncryptionBoxParameters(boolean b) {
        if (b) {
            setFlags(getFlags() | 0x1);
        } else {
            setFlags(getFlags() & (0xffffff ^ 0x1));
        }
    }

}
