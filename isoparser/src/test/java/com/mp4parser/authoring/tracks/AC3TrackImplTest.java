package com.mp4parser.authoring.tracks;

import com.mp4parser.Container;
import com.mp4parser.IsoFile;
import com.mp4parser.authoring.FileDataSourceImpl;
import com.mp4parser.authoring.Movie;
import com.mp4parser.authoring.Track;
import com.mp4parser.authoring.builder.DefaultMp4Builder;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;

public class AC3TrackImplTest {
    @Test
    public void freeze() throws IOException {
        Track t = new AC3TrackImpl(new FileDataSourceImpl(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile() + "/com/mp4parser/authoring/tracks/ac3-sample.ac3"));
        Movie m = new Movie();
        m.addTrack(t);

        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        Container isoFile = mp4Builder.build(m);
        //WritableByteChannel fc = new FileOutputStream("ac3-sample.mp4").getChannel();
        //isoFile.writeContainer(fc);
        //fc.close();
        IsoFile isoFileReference = new IsoFile(new FileInputStream(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile() + "/com/mp4parser/authoring/tracks/ac3-sample.mp4").getChannel());
        BoxComparator.check(isoFile, isoFileReference, "moov[0]/mvhd[0]", "moov[0]/trak[0]/tkhd[0]", "moov[0]/trak[0]/mdia[0]/mdhd[0]");
    }
}
