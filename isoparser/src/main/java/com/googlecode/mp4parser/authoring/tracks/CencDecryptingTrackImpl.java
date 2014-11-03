package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.OriginalFormatBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.SchemeTypeBox;
import com.coremedia.iso.boxes.sampleentry.AudioSampleEntry;
import com.coremedia.iso.boxes.sampleentry.VisualSampleEntry;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import com.googlecode.mp4parser.boxes.cenc.CencDecryptingSampleList;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.CencSampleEncryptionInformationGroupEntry;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.GroupEntry;
import com.googlecode.mp4parser.util.Path;
import com.googlecode.mp4parser.util.RangeStartMap;

import javax.crypto.SecretKey;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.*;

public class CencDecryptingTrackImpl extends AbstractTrack {
    CencDecryptingSampleList samples;
    Track original;
    RangeStartMap<Integer, SecretKey> indexToKey = new RangeStartMap<Integer, SecretKey>();

    public CencDecryptingTrackImpl(CencEncryptedTrack original, SecretKey sk) {
        this(original, Collections.singletonMap(original.getDefaultKeyId(), sk));

    }

    public CencDecryptingTrackImpl(CencEncryptedTrack original, Map<UUID, SecretKey> keys) {
        super("dec(" + original.getName() + ")");
        this.original = original;
        SchemeTypeBox schm = Path.getPath(original.getSampleDescriptionBox(), "enc./sinf/schm");
        if (!("cenc".equals(schm.getSchemeType()) || "cbc1".equals(schm.getSchemeType()))) {
            throw new RuntimeException("You can only use the CencDecryptingTrackImpl with CENC (cenc or cbc1) encrypted tracks");
        }

        List<CencSampleEncryptionInformationGroupEntry> groupEntries = new ArrayList<CencSampleEncryptionInformationGroupEntry>();
        for (Map.Entry<GroupEntry, long[]> groupEntry : original.getSampleGroups().entrySet()) {
            if (groupEntry.getKey() instanceof CencSampleEncryptionInformationGroupEntry) {
                groupEntries.add((CencSampleEncryptionInformationGroupEntry) groupEntry.getKey());
            } else {
                getSampleGroups().put(groupEntry.getKey(), groupEntry.getValue());
            }
        }


        int lastSampleGroupDescriptionIndex = -1;
        for (int i = 0; i < original.getSamples().size(); i++) {
            int index = 0;
            for (int j = 0; j < groupEntries.size(); j++) {
                GroupEntry groupEntry = groupEntries.get(j);
                long[] sampleNums = original.getSampleGroups().get(groupEntry);
                if (Arrays.binarySearch(sampleNums, i) >= 0) {
                    index = j + 1;
                }
            }
            if (lastSampleGroupDescriptionIndex != index) {
                if (index == 0) {
                    // if default_encrypted == false then keys.get(original.getDefaultKeyId()) == null
                    indexToKey.put(i, keys.get(original.getDefaultKeyId()));
                } else {
                    if (groupEntries.get(index - 1).isEncrypted()) {
                        SecretKey sk = keys.get(groupEntries.get(index - 1).getKid());
                        if (sk == null) {
                            throw new RuntimeException("Key " + groupEntries.get(index - 1).getKid() + " was not supplied for decryption");
                        }
                        indexToKey.put(i, sk);
                    } else {
                        indexToKey.put(i, null);
                    }
                }
                lastSampleGroupDescriptionIndex = index;
            }
        }


        samples = new CencDecryptingSampleList(indexToKey, original.getSamples(), original.getSampleEncryptionEntries(), schm.getSchemeType());
    }

    public void close() throws IOException {
        original.close();
    }

    public long[] getSyncSamples() {
        return original.getSyncSamples();
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        OriginalFormatBox frma = Path.getPath(original.getSampleDescriptionBox(), "enc./sinf/frma");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        SampleDescriptionBox stsd;
        try {
            original.getSampleDescriptionBox().getBox(Channels.newChannel(baos));
            stsd = (SampleDescriptionBox) new IsoFile(new MemoryDataSourceImpl(baos.toByteArray())).getBoxes().get(0);
        } catch (IOException e) {
            throw new RuntimeException("Dumping stsd to memory failed");
        }

        if (stsd.getSampleEntry() instanceof AudioSampleEntry) {
            ((AudioSampleEntry) stsd.getSampleEntry()).setType(frma.getDataFormat());
        } else if (stsd.getSampleEntry() instanceof VisualSampleEntry) {
            ((VisualSampleEntry) stsd.getSampleEntry()).setType(frma.getDataFormat());
        } else {
            throw new RuntimeException("I don't know " + stsd.getSampleEntry().getType());
        }
        List<Box> nuBoxes = new LinkedList<Box>();
        for (Box box : stsd.getSampleEntry().getBoxes()) {
            if (!box.getType().equals("sinf")) {
                nuBoxes.add(box);
            }
        }
        stsd.getSampleEntry().setBoxes(nuBoxes);
        return stsd;
    }


    public long[] getSampleDurations() {
        return original.getSampleDurations();
    }

    public TrackMetaData getTrackMetaData() {
        return original.getTrackMetaData();
    }

    public String getHandler() {
        return original.getHandler();
    }

    public List<Sample> getSamples() {
        return samples;
    }

}
