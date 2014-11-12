package com.googlecode.mp4parser;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CencDecryptingTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.CencEncryptingTrackImpl;
import com.googlecode.mp4parser.authoring.tracks.CencEncyprtedTrack;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.UUID;

/**
 * Created by user on 22.07.2014.
 */
public class CencEncryptDecrypt {
    public static void main(String[] args) throws IOException {
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();

        Movie mOrig = MovieCreator.build(CencEncryptDecrypt.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/1365070268951.mp4");

        Movie mEncryptOut = new Movie();
        SecretKey sk = new SecretKeySpec(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, "AES");
        for (Track track : mOrig.getTracks()) {
            mEncryptOut.addTrack(new CencEncryptingTrackImpl(track, UUID.randomUUID(), sk, false));
        }

        Container cEncrypted = mp4Builder.build(mEncryptOut);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cEncrypted.writeContainer(Channels.newChannel(baos));

        Movie mEncryptIn = MovieCreator.build (new MemoryDataSourceImpl(baos.toByteArray()));
        Movie mDecrypt = new Movie();

        for (Track track : mEncryptIn.getTracks()) {
            if (track instanceof CencEncyprtedTrack) {
                mDecrypt.addTrack(new CencDecryptingTrackImpl((CencEncyprtedTrack) track, sk));
            } else {
                mDecrypt.addTrack(track);
            }
        }

        Container cDecrypted = mp4Builder.build(mDecrypt);
        FileOutputStream fos = new FileOutputStream("output.mp4");
        cDecrypted.writeContainer(fos.getChannel());

    }
}
