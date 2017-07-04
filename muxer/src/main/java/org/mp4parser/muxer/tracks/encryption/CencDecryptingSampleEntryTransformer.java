package org.mp4parser.muxer.tracks.encryption;

import org.mp4parser.Box;
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
import org.mp4parser.tools.Path;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

class CencDecryptingSampleEntryTransformer {
    private HashMap<SampleEntry, SampleEntry> cache = new HashMap<>();

    SampleEntry transform(SampleEntry se) {
        SampleEntry decSe = cache.get(se);
        if (decSe == null) {
            OriginalFormatBox frma;
            if (se.getType().equals("enca")) {
                frma = Path.getPath((AudioSampleEntry) se, "sinf/frma");
            } else if (se.getType().equals("encv")) {
                frma = Path.getPath((VisualSampleEntry) se, "sinf/frma");
            } else {
                return se; // it's no encrypted SampleEntry - do nothing
            }
            if (frma == null) {
                throw new RuntimeException("Could not find frma box");
            }


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                // This creates a copy cause I can't change the original instance
                se.getBox(Channels.newChannel(baos));
                decSe = (SampleEntry) new IsoFile(new ByteBufferByteChannel(ByteBuffer.wrap(baos.toByteArray()))).getBoxes().get(0);
            } catch (IOException e) {
                throw new RuntimeException("Dumping stsd to memory failed");
            }

            if (decSe instanceof AudioSampleEntry) {
                ((AudioSampleEntry) decSe).setType(frma.getDataFormat());
            } else if (decSe instanceof VisualSampleEntry) {
                ((VisualSampleEntry) decSe).setType(frma.getDataFormat());
            } else {
                throw new RuntimeException("I don't know " + decSe.getType());
            }

            List<Box> nuBoxes = new LinkedList<>();
            for (Box box : decSe.getBoxes()) {
                if (!box.getType().equals("sinf")) {
                    nuBoxes.add(box);
                }
            }
            decSe.setBoxes(nuBoxes);
            cache.put(se, decSe);
        }
        return decSe;

    }
}
