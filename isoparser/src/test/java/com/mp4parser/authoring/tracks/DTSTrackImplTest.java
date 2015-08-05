package com.mp4parser.authoring.tracks;

import com.mp4parser.IsoFile;
import com.mp4parser.RandomAccessSource;
import com.mp4parser.authoring.FileDataSourceImpl;
import com.mp4parser.authoring.Movie;
import com.mp4parser.authoring.Track;
import com.mp4parser.authoring.builder.DefaultMp4Builder;
import com.mp4parser.authoring.builder.FragmentIntersectionFinder;
import com.mp4parser.authoring.builder.StaticFragmentIntersectionFinderImpl;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.channels.Channels;
import java.util.Collections;

public class DTSTrackImplTest {
    @Test
    public void checkOutputIsStable() throws Exception {
        Movie m = new Movie();
        DTSTrackImpl dts = new DTSTrackImpl(new FileDataSourceImpl(DTSTrackImplTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/com/mp4parser/authoring/tracks/dts-sample.dtshd"));
        m.addTrack(dts);
        FragmentIntersectionFinder fif = new StaticFragmentIntersectionFinderImpl(Collections.singletonMap((Track)dts, new long[]{1}));
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        mp4Builder.setIntersectionFinder(fif);
        RandomAccessSource.Container c = mp4Builder.build(m);

        // c.writeContainer(new FileOutputStream("C:\\dev\\mp4parser\\isoparser\\src\\test\\resources\\com\\googlecode\\mp4parser\\authoring\\tracks\\dts-sample.mp4").getChannel());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        c.writeContainer(Channels.newChannel(baos));
        IsoFile ref = new IsoFile(
                new FileInputStream(DTSTrackImplTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/com/mp4parser/authoring/tracks/dts-sample.mp4").getChannel());
        BoxComparator.check(ref, c, "moov[0]/mvhd[0]", "moov[0]/trak[0]/tkhd[0]", "moov[0]/trak[0]/mdia[0]/mdhd[0]");


    }
}