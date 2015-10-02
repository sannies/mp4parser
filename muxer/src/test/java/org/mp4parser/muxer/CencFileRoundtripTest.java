package org.mp4parser.muxer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mp4parser.Container;
import org.mp4parser.boxes.samplegrouping.CencSampleEncryptionInformationGroupEntry;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.builder.FragmentedMp4Builder;
import org.mp4parser.muxer.builder.Mp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.CencDecryptingTrackImpl;
import org.mp4parser.muxer.tracks.CencEncryptedTrack;
import org.mp4parser.muxer.tracks.CencEncryptingTrackImpl;
import org.mp4parser.tools.ByteBufferByteChannel;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.*;

public class CencFileRoundtripTest {
    String baseDir = CencFileRoundtripTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    Map<UUID, SecretKey> keys;
    HashMap<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation1;
    HashMap<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation2;
    HashMap<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation3;
    UUID uuidDefault;

    @Before
    public void setUp() throws Exception {
        uuidDefault = UUID.randomUUID();
        UUID uuidAlt = UUID.randomUUID();
        SecretKey cekDefault = new SecretKeySpec(new byte[]{0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, "AES");
        SecretKey cekAlt = new SecretKeySpec(new byte[]{0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, "AES");

        keys = new HashMap<UUID, SecretKey>();
        keys.put(uuidDefault, cekDefault);
        keys.put(uuidAlt, cekAlt);

        CencSampleEncryptionInformationGroupEntry cencNone = new CencSampleEncryptionInformationGroupEntry();
        cencNone.setEncrypted(false);

        CencSampleEncryptionInformationGroupEntry cencAlt = new CencSampleEncryptionInformationGroupEntry();
        cencAlt.setKid(uuidAlt);
        cencAlt.setIvSize(8);
        cencAlt.setEncrypted(true);

        CencSampleEncryptionInformationGroupEntry cencDefault = new CencSampleEncryptionInformationGroupEntry();
        cencAlt.setKid(uuidDefault);
        cencAlt.setIvSize(8);
        cencAlt.setEncrypted(true);
        keyRotation1 = new HashMap<CencSampleEncryptionInformationGroupEntry, long[]>();
        keyRotation1.put(cencNone, new long[]{0, 1, 2, 3, 4});
        keyRotation1.put(cencAlt, new long[]{10, 11, 12, 13});

        keyRotation2 = new HashMap<CencSampleEncryptionInformationGroupEntry, long[]>();
        keyRotation2.put(cencNone, new long[]{0, 2, 4, 6, 8});

        keyRotation3 = new HashMap<CencSampleEncryptionInformationGroupEntry, long[]>();
        keyRotation3.put(cencDefault, new long[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15});

    }

    @Test
    public void testDefaultPlainFragMp4_cbc1() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cbc1", null, false);
    }

    @Test
    public void testDefaultPlainFragMp4_cenc() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cenc", null, false);
    }

    @Test
    public void testDefaultPlainStdMp4_cbc1() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cbc1", null, false);
    }

    @Test
    public void testDefaultPlainStdMp4_cenc() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cenc", null, false);
    }

    @Test
    public void testSingleKeyMp4_cbc1() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, null, "cbc1", uuidDefault, false);
    }

    @Test
    public void testSingleKeyMp4_cenc() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, null, "cenc", uuidDefault, false);
    }

    @Test
    public void testSingleKeyFragMp4_cenc() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, null, "cenc", uuidDefault, false);
    }

    @Test
    public void testMultipleKeysStdMp4_cbc1() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cbc1", uuidDefault, false);
    }


    @Test
    public void testMultipleKeysFragMp4_cbc1() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cbc1", uuidDefault, false);
    }

    @Test
    public void testMultipleKeysStdMp4_2_cbc1() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cbc1", uuidDefault, false);
    }

    @Test
    public void testMultipleKeysFragMp4_2_cbc1() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cbc1", uuidDefault, false);
    }

    @Test
    public void testMultipleKeysStdMp4_cenc() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cenc", uuidDefault, false);
    }

    @Test
    public void testMultipleKeysFragMp4_cenc() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cenc", uuidDefault, false);
    }

    @Test
    public void testMultipleKeysStdMp4_2_cenc() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cenc", uuidDefault, false);
    }

    @Test
    public void testMultipleKeysFragMp4_2_cenc() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cenc", uuidDefault, false);
    }


    @Test
    public void testMultipleKeysFragMp4_2_cenc_pseudo_encrypted() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cenc", uuidDefault, true);
    }

    public void testMultipleKeys(Mp4Builder builder, String testFile, Map<UUID, SecretKey> keys,
                                 HashMap<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation,
                                 String encAlgo, UUID uuidDefault, boolean encryptButClear) throws IOException {
        Movie m1 = MovieCreator.build(testFile);
        Movie m2 = new Movie();
        for (Track track : m1.getTracks()) {

            CencEncryptingTrackImpl cencEncryptingTrack = new CencEncryptingTrackImpl(track, uuidDefault, keys, keyRotation, encAlgo, false, encryptButClear);
            m2.addTrack(cencEncryptingTrack);
        }
        Container c = builder.build(m2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        c.writeContainer(Channels.newChannel(baos));
        new FileOutputStream("c:\\dev\\mp4parser\\m2.mp4").write(baos.toByteArray());

        Movie m3 = MovieCreator.build(new ByteBufferByteChannel(baos.toByteArray()), new InMemRandomAccessSourceImpl(baos.toByteArray()), "inmem");

        Movie m4 = new Movie();
        for (Track track : m3.getTracks()) {
            CencDecryptingTrackImpl cencDecryptingTrack =
                    new CencDecryptingTrackImpl((CencEncryptedTrack) track, keys);
            m4.addTrack(cencDecryptingTrack);
        }
        Container c2 = builder.build(m4);

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        c2.writeContainer(Channels.newChannel(baos2));
        Movie m5 = MovieCreator.build(new ByteBufferByteChannel(baos2.toByteArray()), new InMemRandomAccessSourceImpl(baos2.toByteArray()), "inmem");

        Iterator<Track> tracksPlainIter = m1.getTracks().iterator();
        Iterator<Track> roundTrippedTracksIter = m5.getTracks().iterator();

        while (tracksPlainIter.hasNext() && roundTrippedTracksIter.hasNext()) {
            verifySampleEquality(
                    tracksPlainIter.next().getSamples(),
                    roundTrippedTracksIter.next().getSamples());
        }

    }

    public void verifySampleEquality(List<Sample> orig, List<Sample> roundtripped) throws IOException {
        int i = 0;
        Iterator<Sample> origIter = orig.iterator();
        Iterator<Sample> roundTrippedIter = roundtripped.iterator();
        while (origIter.hasNext() && roundTrippedIter.hasNext()) {
            ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
            ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
            origIter.next().writeTo(Channels.newChannel(baos1));
            roundTrippedIter.next().writeTo(Channels.newChannel(baos2));
            Assert.assertArrayEquals("Sample " + i + " differs", baos1.toByteArray(), baos2.toByteArray());
            i++;
        }

    }
}
