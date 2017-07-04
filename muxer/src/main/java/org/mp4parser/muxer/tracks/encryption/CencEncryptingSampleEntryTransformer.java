package org.mp4parser.muxer.tracks.encryption;

import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.OriginalFormatBox;
import org.mp4parser.boxes.iso14496.part12.ProtectionSchemeInformationBox;
import org.mp4parser.boxes.iso14496.part12.SchemeInformationBox;
import org.mp4parser.boxes.iso14496.part12.SchemeTypeBox;
import org.mp4parser.boxes.iso23001.part7.TrackEncryptionBox;
import org.mp4parser.boxes.sampleentry.AudioSampleEntry;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.sampleentry.VisualSampleEntry;
import org.mp4parser.tools.ByteBufferByteChannel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.UUID;


public class CencEncryptingSampleEntryTransformer  {
    private HashMap<SampleEntry, SampleEntry> cache = new HashMap<>();

    public SampleEntry transform(SampleEntry se, String encryptionAlgo, UUID defaultKeyId) {
        SampleEntry encSampleEntry = cache.get(se);
        if (encSampleEntry == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                se.getBox(Channels.newChannel(baos));
                encSampleEntry= (SampleEntry) new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray()))).getBoxes().get(0);
            } catch (IOException e) {
                throw new RuntimeException("Dumping stsd to memory failed");
            }
            // stsd is now a copy of the original stsd. Not very efficient but we don't have to do that a hundred times ...

            OriginalFormatBox originalFormatBox = new OriginalFormatBox();
            originalFormatBox.setDataFormat(se.getType());
            ProtectionSchemeInformationBox sinf = new ProtectionSchemeInformationBox();
            sinf.addBox(originalFormatBox);

            SchemeTypeBox schm = new SchemeTypeBox();
            schm.setSchemeType(encryptionAlgo);
            schm.setSchemeVersion(0x00010000);
            sinf.addBox(schm);

            SchemeInformationBox schi = new SchemeInformationBox();
            TrackEncryptionBox trackEncryptionBox = new TrackEncryptionBox();
            trackEncryptionBox.setDefaultIvSize(8);
            trackEncryptionBox.setDefaultAlgorithmId(0x01);
            trackEncryptionBox.setDefault_KID(defaultKeyId);
            schi.addBox(trackEncryptionBox);

            sinf.addBox(schi);


            if (se instanceof AudioSampleEntry) {
                ((AudioSampleEntry) encSampleEntry).setType("enca");
                ((AudioSampleEntry) encSampleEntry).addBox(sinf);
            } else if (se instanceof VisualSampleEntry) {
                ((VisualSampleEntry) encSampleEntry).setType("encv");
                ((VisualSampleEntry) encSampleEntry).addBox(sinf);
            } else {
                throw new RuntimeException("I don't know how to cenc " + se.getType());
            }
            cache.put(se, encSampleEntry);
        }
        return encSampleEntry;
    }
}
