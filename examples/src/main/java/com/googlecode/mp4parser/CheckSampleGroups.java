package com.googlecode.mp4parser;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.CencSampleEncryptionInformationGroupEntry;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by sannies on 26.09.2014.
 */
public class CheckSampleGroups {
    public static void main(String[] args) throws IOException {
        Movie m = MovieCreator.build("C:\\dev\\mp4parser\\examples\\src\\main\\resources\\count-video.mp4");

        CencSampleEncryptionInformationGroupEntry cencGroupEntry = new CencSampleEncryptionInformationGroupEntry();
        cencGroupEntry.setEncrypted(true);
        cencGroupEntry.setIvSize(8);
        cencGroupEntry.setKid(UUID.randomUUID());
        m.getTracks().get(0).getSampleGroups().put(cencGroupEntry, new long[]{5, 50});

        DefaultMp4Builder builder = new DefaultMp4Builder();
        Container c = builder.build(m);
        c.writeContainer(new FileOutputStream("output.mp4").getChannel());

    }
}
