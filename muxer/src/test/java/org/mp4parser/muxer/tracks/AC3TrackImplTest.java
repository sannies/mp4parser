package org.mp4parser.muxer.tracks;

import org.junit.Test;
import org.mp4parser.Container;
import org.mp4parser.IsoFile;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.support.BoxComparator;

import java.io.FileInputStream;
import java.io.IOException;

public class AC3TrackImplTest {
    @Test
    public void freeze() throws IOException {
        Track t = new AC3TrackImpl(new FileDataSourceImpl(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile() + "/org/mp4parser/muxer/tracks/ac3-sample.ac3"));
        Movie m = new Movie();
        m.addTrack(t);

        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        Container isoFile = mp4Builder.build(m);
        //WritableByteChannel fc = new FileOutputStream("ac3-sample.mp4").getChannel();
        //isoFile.writeContainer(fc);
        //fc.close();
        IsoFile isoFileReference = new IsoFile(new FileInputStream(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile() + "/org/mp4parser/muxer/tracks/ac3-sample.mp4").getChannel());
        BoxComparator.check(isoFile, isoFileReference, "moov[0]/mvhd[0]", "moov[0]/trak[0]/tkhd[0]", "moov[0]/trak[0]/mdia[0]/mdhd[0]");
    }
}
