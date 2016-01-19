package org.mp4parser.muxer.tracks;

import org.junit.Test;
import org.mp4parser.Container;
import org.mp4parser.IsoFile;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.muxer.builder.Fragmenter;
import org.mp4parser.muxer.builder.StaticFragmentIntersectionFinderImpl;
import org.mp4parser.support.BoxComparator;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.channels.Channels;
import java.util.Collections;

public class DTSTrackImplTest {
    @Test
    public void checkOutputIsStable() throws Exception {
        Movie m = new Movie();
        DTSTrackImpl dts = new DTSTrackImpl(new FileDataSourceImpl(DTSTrackImplTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/org/mp4parser/muxer/tracks/dts-sample.dtshd"));
        m.addTrack(dts);
        Fragmenter fif = new StaticFragmentIntersectionFinderImpl(Collections.singletonMap((Track) dts, new long[]{1}));
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        mp4Builder.setFragmenter(fif);
        Container c = mp4Builder.build(m);

        // c.writeContainer(new FileOutputStream("C:\\dev\\mp4parser\\isoparser\\src\\test\\resources\\com\\googlecode\\mp4parser\\authoring\\tracks\\dts-sample.mp4").getChannel());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        c.writeContainer(Channels.newChannel(baos));
        IsoFile ref = new IsoFile(
                new FileInputStream(DTSTrackImplTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/org/mp4parser/muxer/tracks/dts-sample.mp4").getChannel());
        BoxComparator.check(ref, c, "moov[0]/mvhd[0]", "moov[0]/trak[0]/tkhd[0]", "moov[0]/trak[0]/mdia[0]/mdhd[0]");


    }
}