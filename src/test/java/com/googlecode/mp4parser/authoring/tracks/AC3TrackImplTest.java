package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Arrays;

public class AC3TrackImplTest {
    @Test
    public void freeze() throws IOException {
        Track t = new AC3TrackImpl(new BufferedInputStream(AC3TrackImpl.class.getResourceAsStream("/com/googlecode/mp4parser/authoring/tracks/ac3-sample.ac3")));
        Movie m = new Movie();
        m.addTrack(t);

        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        IsoFile isoFile = mp4Builder.build(m);

//        FileChannel fc = new FileOutputStream("/home/sannies/scm/svn/mp4parser/isoparser/src/test/resources/com/googlecode/mp4parser/authoring/tracks/ac3-sample.mp4").getChannel();
//        isoFile.getBox(fc);
//        fc.close();
        IsoFile isoFileReference = new IsoFile(Channels.newChannel(AACTrackImplTest.class.getResourceAsStream("/com/googlecode/mp4parser/authoring/tracks/ac3-sample.mp4")));
        BoxComparator.check(isoFile, isoFileReference, "/moov[0]/mvhd[0]", "/moov[0]/trak[0]/tkhd[0]", "/moov[0]/trak[0]/mdia[0]/mdhd[0]");
    }
}
