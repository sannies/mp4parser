package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.Mp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import org.junit.Test;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class CencTracksImplTest {


    @Test
    public void testEncryptDecryptDefaultMp4() throws Exception {
        SecretKey sk = new SecretKeySpec(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, "AES");
        Movie m = MovieCreator.build(
                CencTracksImplTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() +
                        "/com/googlecode/mp4parser/authoring/samples/1365070268951.mp4");

        List<Track> encTracks = new LinkedList<Track>();
        for (Track track : m.getTracks()) {
            encTracks.add(new CencEncryptingTrackImpl(track, UUID.randomUUID(), sk, false));
        }
        m.setTracks(encTracks);

        Mp4Builder mp4Builder = new DefaultMp4Builder();
        Container c = mp4Builder.build(m);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        c.writeContainer(Channels.newChannel(baos));

        //c.writeContainer(new FileOutputStream("output.mp4").getChannel());

        Movie m2 = MovieCreator.build(new MemoryDataSourceImpl(baos.toByteArray()));
        List<Track> decTracks = new LinkedList<Track>();
        for (Track track : m2.getTracks()) {
            decTracks.add(new CencDecryptingTrackImpl((CencEncryptedTrack) track, sk));
        }
        m2.setTracks(decTracks);
        c = mp4Builder.build(m2);

        //c.writeContainer(new FileOutputStream("output2.mp4").getChannel());


    }
    @Test
    public void testEncryptDecryptFragmentedMp4() throws Exception {
        SecretKey sk = new SecretKeySpec(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, "AES");
        Movie m = MovieCreator.build(
                CencTracksImplTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() +
                        "/com/googlecode/mp4parser/authoring/samples/1365070268951.mp4");

        List<Track> encTracks = new LinkedList<Track>();
        for (Track track : m.getTracks()) {
            encTracks.add(new CencEncryptingTrackImpl(track, UUID.randomUUID(), sk, false));
        }
        m.setTracks(encTracks);

        Mp4Builder mp4Builder = new FragmentedMp4Builder();
        Container c = mp4Builder.build(m);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        c.writeContainer(Channels.newChannel(baos));

        //c.writeContainer(new FileOutputStream("output.mp4").getChannel());

        Movie m2 = MovieCreator.build(new MemoryDataSourceImpl(baos.toByteArray()));
        List<Track> decTracks = new LinkedList<Track>();
        for (Track track : m2.getTracks()) {
            decTracks.add(new CencDecryptingTrackImpl((CencEncryptedTrack) track, sk));
        }
        m2.setTracks(decTracks);
        c = mp4Builder.build(m2);

        //c.writeContainer(new FileOutputStream("output2.mp4").getChannel());

    }
}
