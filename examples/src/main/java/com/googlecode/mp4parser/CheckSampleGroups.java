package com.googlecode.mp4parser;

import com.mp4parser.Container;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.builder.DefaultMp4Builder;
import com.mp4parser.muxer.container.mp4.MovieCreator;
import com.mp4parser.muxer.tracks.CencEncryptingTrackImpl;
import com.mp4parser.boxes.samplegrouping.CencSampleEncryptionInformationGroupEntry;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by sannies on 26.09.2014.
 */
public class CheckSampleGroups {
    public static void main(String[] args) throws IOException {
        Movie m = MovieCreator.build("C:\\dev\\mp4parser\\examples\\src\\main\\resources\\count-video.mp4");

        UUID uuid1 = UUID.randomUUID();
        UUID uuid2 = UUID.randomUUID();
        SecretKey sk1 = new SecretKeySpec(new byte[]{0, 1, 1, 2, 0, 1, 1, 2, 0, 1, 1, 2, 0, 1, 1, 2}, "AES");
        SecretKey sk2 = new SecretKeySpec(new byte[]{4, 1, 1, 2, 0, 1, 1, 2, 0, 1, 1, 2, 0, 1, 1, 2}, "AES");

        HashMap<UUID, SecretKey> keys = new HashMap<UUID, SecretKey>();
        keys.put(uuid1, sk1);
        keys.put(uuid2, sk2);

        CencSampleEncryptionInformationGroupEntry cencGroupEntry = new CencSampleEncryptionInformationGroupEntry();
        cencGroupEntry.setEncrypted(true);
        cencGroupEntry.setIvSize(8);
        cencGroupEntry.setKid(UUID.randomUUID());
        m.getTracks().get(0).getSampleGroups().put(cencGroupEntry, new long[]{5, 6, 50});

        DefaultMp4Builder builder = new DefaultMp4Builder();
        Map<CencSampleEncryptionInformationGroupEntry, long[]> keyRotation =
                new HashMap<CencSampleEncryptionInformationGroupEntry, long[]>();
        keyRotation.put(cencGroupEntry, new long[]{5, 6, 50});
        m.setTracks(Collections.<Track>singletonList(
                new CencEncryptingTrackImpl(
                        m.getTracks().get(0),
                        uuid1, keys, keyRotation, "cenc", false))); // cbc1 alternatively
        Container c = builder.build(m);
        c.writeContainer(new FileOutputStream("output.mp4").getChannel());

    }
}
