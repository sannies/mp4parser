package com.googlecode.mp4parser.boxes.cenc;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.Mp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CencDecryptingTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.CencEncryptingTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.CencEncyprtedTrack;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.CencSampleEncryptionInformationGroupEntry;
import org.junit.Assert;
import org.junit.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.*;

/**
 * Created by sannies on 27.09.2014.
 */
public class CencFileRoundtripTest {
    String baseDir = CencFileRoundtripTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();

    @Test
    public void testMultipleKeysStdMp4() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4");
    }


    @Test
    public void testMultipleKeysFragMp4() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4");
    }

    public void testMultipleKeys(Mp4Builder builder, String testFile) throws IOException {
        Movie m1 = MovieCreator.build(testFile);


        UUID uuidDefault = UUID.randomUUID();
        UUID uuidAlt = UUID.randomUUID();
        SecretKey cekDefault = new SecretKeySpec(new byte[]{0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, "AES");
        SecretKey cekAlt = new SecretKeySpec(new byte[]{0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, "AES");

        Map<UUID, SecretKey> keys = new HashMap<UUID, SecretKey>();
        keys.put(uuidDefault, cekDefault);
        keys.put(uuidAlt, cekAlt);

        Movie m2 = new Movie();
        for (Track track : m1.getTracks()) {
            CencSampleEncryptionInformationGroupEntry cencNone = new CencSampleEncryptionInformationGroupEntry();
            cencNone.setEncrypted(false);
            CencSampleEncryptionInformationGroupEntry cencAlt = new CencSampleEncryptionInformationGroupEntry();
            cencAlt.setKid(uuidAlt);
            cencAlt.setIvSize(8);
            cencAlt.setEncrypted(true);
            HashMap<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation = new HashMap<CencSampleEncryptionInformationGroupEntry, long[]>();
            keyRotation.put(cencNone, new long[]{0, 1, 2, 3, 4});
            keyRotation.put(cencAlt, new long[]{10, 11, 12, 13});
            CencEncryptingTrackImpl cencEncryptingTrack = new CencEncryptingTrackImpl(track, uuidDefault, keys, keyRotation);
            m2.addTrack(cencEncryptingTrack);
        }
        Container c = builder.build(m2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        c.writeContainer(Channels.newChannel(baos));

        Movie m3 = MovieCreator.build(new MemoryDataSourceImpl(baos.toByteArray()));

        Movie m4 = new Movie();
        for (Track track : m3.getTracks()) {
            CencDecryptingTrackImpl cencDecryptingTrack =
                    new CencDecryptingTrackImpl((CencEncyprtedTrack) track, keys);
            m4.addTrack(cencDecryptingTrack);
        }
        Container c2 = builder.build(m4);

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        c2.writeContainer(Channels.newChannel(baos2));
        Movie m5 = MovieCreator.build(new MemoryDataSourceImpl(baos2.toByteArray()));

        Iterator<Track> tracksPlainIter = m1.getTracks().iterator();
        Iterator<Track> roundTrippedTracksIter = m5.getTracks().iterator();

        int trackNo = 0;
        while (tracksPlainIter.hasNext() && roundTrippedTracksIter.hasNext()) {
            System.err.println("Track: " + trackNo++);
            verifySampleEquality(
                    tracksPlainIter.next().getSamples(),
                    roundTrippedTracksIter.next().getSamples());
        }

    }

    public void verifySampleEquality(List<Sample> orig, List<Sample> roundtripped) throws IOException {
        Iterator<Sample> origIter = orig.iterator();
        Iterator<Sample> roundTrippedIter = roundtripped.iterator();
        int sampleNo = 0;
        while (origIter.hasNext() && roundTrippedIter.hasNext()) {
            System.err.println("Sample: " + sampleNo++);
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            origIter.next().writeTo(Channels.newChannel(baos1));
            roundTrippedIter.next().writeTo(Channels.newChannel(baos2));
            Assert.assertArrayEquals(baos1.toByteArray(), baos2.toByteArray());
        }

    }
}
