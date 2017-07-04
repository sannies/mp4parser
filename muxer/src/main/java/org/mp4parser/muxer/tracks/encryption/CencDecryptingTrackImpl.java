package org.mp4parser.muxer.tracks.encryption;

import org.mp4parser.Container;
import org.mp4parser.boxes.iso23001.part7.TrackEncryptionBox;
import org.mp4parser.boxes.sampleentry.SampleEntry;
import org.mp4parser.boxes.samplegrouping.GroupEntry;
import org.mp4parser.muxer.AbstractTrack;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.TrackMetaData;
import org.mp4parser.tools.Path;
import org.mp4parser.tools.RangeStartMap;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.*;

public class CencDecryptingTrackImpl extends AbstractTrack {
    private CencDecryptingSampleList samples;
    private CencEncryptedTrack original;
    private LinkedHashSet<SampleEntry> sampleEntries = new LinkedHashSet<>();



    public CencDecryptingTrackImpl(CencEncryptedTrack original, SecretKey sk) {
        super("dec(" + original.getName() + ")");
        this.original = original;
        Map<UUID, SecretKey> keys = new HashMap<>();
        for (SampleEntry sampleEntry : original.getSampleEntries()) {
            TrackEncryptionBox tenc = Path.getPath((Container)sampleEntry, "sinf[0]/schi[0]/tenc[0]");
            assert tenc != null;
            keys.put(tenc.getDefault_KID(), sk);
        }
        init(keys);

    }

    public CencDecryptingTrackImpl(CencEncryptedTrack original, Map<UUID, SecretKey> keys) {
        super("dec(" + original.getName() + ")");
        this.original = original;
        init(keys);
    }

    private void init(Map<UUID, SecretKey> keys) {
        CencDecryptingSampleEntryTransformer tx = new CencDecryptingSampleEntryTransformer();
        List<Sample> encSamples = original.getSamples();

        RangeStartMap<Integer, SecretKey> indexToKey = new RangeStartMap<>();
        RangeStartMap<Integer, SampleEntry> indexToSampleEntry = new RangeStartMap<>();
        SampleEntry previousSampleEntry = null;

        for (int i = 0; i < encSamples.size(); i++) {
            Sample encSample = encSamples.get(i);
            SampleEntry current = encSample.getSampleEntry();
            sampleEntries.add(tx.transform(encSample.getSampleEntry()));
            if (previousSampleEntry != current) {
                indexToSampleEntry.put(i, current);
                TrackEncryptionBox tenc = Path.getPath((Container) encSample.getSampleEntry(), "sinf[0]/schi[0]/tenc[0]");
                if (tenc != null) {
                    indexToKey.put(i, keys.get(tenc.getDefault_KID()));
                } else {
                    indexToKey.put(i, null);
                }
            }
            previousSampleEntry = current;
        }


        samples = new CencDecryptingSampleList(indexToKey, indexToSampleEntry, encSamples, original.getSampleEncryptionEntries());

    }

    public void close() throws IOException {
        original.close();
    }

    public long[] getSyncSamples() {
        return original.getSyncSamples();
    }

    public List<SampleEntry> getSampleEntries() {
        return new ArrayList<>(sampleEntries);
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

    @Override
    public Map<GroupEntry, long[]> getSampleGroups() {
        return original.getSampleGroups();
    }
}
