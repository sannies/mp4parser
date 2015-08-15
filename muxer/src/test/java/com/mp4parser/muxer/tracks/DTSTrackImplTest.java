package com.mp4parser.muxer.tracks;

import com.mp4parser.Container;
import com.mp4parser.IsoFile;
import com.mp4parser.muxer.FileDataSourceImpl;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.builder.DefaultMp4Builder;
import com.mp4parser.muxer.builder.Fragmenter;
import com.mp4parser.muxer.builder.StaticFragmentIntersectionFinderImpl;
import com.mp4parser.support.BoxComparator;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.channels.Channels;
import java.util.Collections;

public class DTSTrackImplTest {
    @Test
    public void checkOutputIsStable() throws Exception {
        Movie m = new Movie();
        DTSTrackImpl dts = new DTSTrackImpl(new FileDataSourceImpl(DTSTrackImplTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/com/mp4parser/muxer/tracks/dts-sample.dtshd"));
        m.addTrack(dts);
        Fragmenter fif = new StaticFragmentIntersectionFinderImpl(Collections.singletonMap((Track) dts, new long[]{1}));
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        mp4Builder.setFragmenter(fif);
        Container c = mp4Builder.build(m);

        // c.writeContainer(new FileOutputStream("C:\\dev\\mp4parser\\isoparser\\src\\test\\resources\\com\\googlecode\\mp4parser\\authoring\\tracks\\dts-sample.mp4").getChannel());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        c.writeContainer(Channels.newChannel(baos));
        IsoFile ref = new IsoFile(
                new FileInputStream(DTSTrackImplTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/com/mp4parser/muxer/tracks/dts-sample.mp4").getChannel());
        BoxComparator.check(ref, c, "moov[0]/mvhd[0]", "moov[0]/trak[0]/tkhd[0]", "moov[0]/trak[0]/mdia[0]/mdhd[0]");


    }
}