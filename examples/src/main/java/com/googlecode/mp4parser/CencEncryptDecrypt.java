package com.googlecode.mp4parser;

import com.mp4parser.Container;
import com.mp4parser.muxer.InMemRandomAccessSourceImpl;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.builder.DefaultMp4Builder;
import com.mp4parser.muxer.container.mp4.MovieCreator;
import com.mp4parser.muxer.tracks.CencDecryptingTrackImpl;
import com.mp4parser.muxer.tracks.CencEncryptedTrack;
import com.mp4parser.muxer.tracks.CencEncryptingTrackImpl;
import com.mp4parser.tools.ByteBufferByteChannel;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.UUID;

public class CencEncryptDecrypt {
    public static void main(String[] args) throws IOException {
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();

        Movie mOrig = MovieCreator.build(CencEncryptDecrypt.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/1365070268951.mp4");


        Movie mEncryptOut = new Movie();
        SecretKey sk = new SecretKeySpec(new byte[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0}, "AES");
        for (Track track : mOrig.getTracks()) {
            mEncryptOut.addTrack(new CencEncryptingTrackImpl(track, UUID.randomUUID(), sk, true));
        }

        Container cEncrypted = mp4Builder.build(mEncryptOut);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        cEncrypted.writeContainer(Channels.newChannel(baos));

        FileOutputStream fos = new FileOutputStream("output-enc.mp4");
        fos.write(baos.toByteArray());

        Movie mEncryptIn = MovieCreator.build (new ByteBufferByteChannel(baos.toByteArray()), new InMemRandomAccessSourceImpl(baos.toByteArray()), "inmem");
        Movie mDecrypt = new Movie();

        for (Track track : mEncryptIn.getTracks()) {
            if (track instanceof CencEncryptedTrack) {
                mDecrypt.addTrack(new CencDecryptingTrackImpl((CencEncryptedTrack) track, sk));
            } else {
                mDecrypt.addTrack(track);
            }
        }

        Container cDecrypted = mp4Builder.build(mDecrypt);
        FileOutputStream fos2 = new FileOutputStream("output.mp4");
        cDecrypted.writeContainer(fos2.getChannel());

    }
}
