package org.mp4parser.muxer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mp4parser.Container;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.builder.FragmentedMp4Builder;
import org.mp4parser.muxer.builder.Mp4Builder;
import org.mp4parser.muxer.container.mp4.MovieCreator;
import org.mp4parser.muxer.tracks.encryption.CencDecryptingTrackImpl;
import org.mp4parser.muxer.tracks.encryption.CencEncryptedTrack;
import org.mp4parser.muxer.tracks.encryption.CencEncryptingTrackImpl;
import org.mp4parser.tools.ByteBufferByteChannel;
import org.mp4parser.tools.RangeStartMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.*;

public class CencFileRoundtripTest {
    private String baseDir = CencFileRoundtripTest.class.getProtectionDomain().getCodeSource().getLocation().getFile();
    private Map<UUID, SecretKey> keys;
    private RangeStartMap<Integer, UUID> keyRotation1;
    private RangeStartMap<Integer, UUID> keyRotation2;
    private RangeStartMap<Integer, UUID> keyRotation3;


    @BeforeEach
    public void setUp() throws Exception {
        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();

        SecretKey cek1 = new SecretKeySpec(new byte[]{0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, "AES");
        SecretKey cek2 = new SecretKeySpec(new byte[]{0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1}, "AES");

        keys = new HashMap<>();
        keys.put(uuid1, cek1);
        keys.put(uuid2, cek2);

        keyRotation1 = new RangeStartMap<>();
        keyRotation1.put(0, uuid1);

        keyRotation2 = new RangeStartMap<>();
        keyRotation2.put(0, null);
        keyRotation2.put(24, uuid1);


        keyRotation3 = new RangeStartMap<>();
        keyRotation3.put(0, null);
        keyRotation3.put(24, uuid1);
        keyRotation3.put(48, uuid2);

    }

    @Test
    public void testSingleKeysStdMp4_cbc1() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cbc1",  false);
    }


    @Test
    public void testSingleKeysFragMp4_cbc1() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cbc1",  false);
    }

    @Test
    public void testSingleKeysStdMp4_cenc() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cenc",  false);
    }

    @Test
    public void testSingleKeysFragMp4_cenc() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation1, "cenc",  false);
    }


    @Test
    public void testClearLeadStdMp4_2_cbc1() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cbc1",  false);
    }

    @Test
    public void testClearLeadFragMp4_2_cbc1() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cbc1",  false);
    }


    @Test
    public void testClearLeadStdMp4_2_cenc() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cenc", false);
    }

    @Test
    public void testClearLeadFragMp4_2_cenc() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cenc", false);
    }


    @Test
    public void testMultipleKeysStdMp4_2_cbc1() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cbc1",  false);
    }

    @Test
    public void testMultipleKeysFragMp4_2_cbc1() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cbc1",  false);
    }


    @Test
    public void testMultipleKeysStdMp4_2_cenc() throws IOException {
        testMultipleKeys(new DefaultMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cenc", false);
    }

    @Test
    public void testMultipleKeysFragMp4_2_cenc() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation3, "cenc", false);
    }





    @Test
    public void testMultipleKeysFragMp4_2_cenc_pseudo_encrypted() throws IOException {
        testMultipleKeys(new FragmentedMp4Builder(), baseDir + "/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4", keys, keyRotation2, "cenc", true);
    }

    private void testMultipleKeys(Mp4Builder builder, String testFile, Map<UUID, SecretKey> keys,
                                  RangeStartMap<Integer, UUID> keyRotation,
                                  String encAlgo, boolean encryptButClear) throws IOException {
        Movie m1 = MovieCreator.build(testFile);
        Movie m2 = new Movie();
        for (Track track : m1.getTracks()) {
            CencEncryptingTrackImpl cencEncryptingTrack =
                    new CencEncryptingTrackImpl(track, keyRotation, keys, encAlgo,false, encryptButClear);
            m2.addTrack(cencEncryptingTrack);
        }
        Container c = builder.build(m2);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        c.writeContainer(Channels.newChannel(baos));
        new FileOutputStream("m2.mp4").write(baos.toByteArray());

        Movie m3 = MovieCreator.build(
                new ByteBufferByteChannel(baos.toByteArray()),
                new InMemRandomAccessSourceImpl(baos.toByteArray()), "inmem");

        Movie m4 = new Movie();
        for (Track track : m3.getTracks()) {
            CencDecryptingTrackImpl cencDecryptingTrack =
                    new CencDecryptingTrackImpl((CencEncryptedTrack) track, keys);
            m4.addTrack(cencDecryptingTrack);
        }
        Container c2 = builder.build(m4);

        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        c2.writeContainer(Channels.newChannel(baos2));
        Movie m5 = MovieCreator.build(
                new ByteBufferByteChannel(baos2.toByteArray()),
                new InMemRandomAccessSourceImpl(baos2.toByteArray()), "inmem");

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
            Assertions.assertArrayEquals(baos1.toByteArray(), baos2.toByteArray(), "Sample " + i + " differs");
            i++;
        }

    }
}
