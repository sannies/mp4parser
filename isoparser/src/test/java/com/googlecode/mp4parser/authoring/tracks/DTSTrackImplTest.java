package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.Fragmenter;
import com.googlecode.mp4parser.authoring.builder.StaticFragmentIntersectionFinderImpl;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.nio.channels.Channels;
import java.util.Collections;

public class DTSTrackImplTest {
    @Test
    public void checkOutputIsStable() throws Exception {
        Movie m = new Movie();
        DTSTrackImpl dts = new DTSTrackImpl(new FileDataSourceImpl(DTSTrackImplTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/com/googlecode/mp4parser/authoring/tracks/dts-sample.dtshd"));
        m.addTrack(dts);
        Fragmenter fif = new StaticFragmentIntersectionFinderImpl(Collections.singletonMap((Track)dts, new long[]{1}));
        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        mp4Builder.setFragmenter(fif);
        Container c = mp4Builder.build(m);


        //c.writeContainer(new FileOutputStream("C:\\dev\\mp4parser\\isoparser\\src\\test\\resources\\com\\googlecode\\mp4parser\\authoring\\tracks\\dts-sample.mp4").getChannel());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        c.writeContainer(Channels.newChannel(baos));
        IsoFile ref = new IsoFile(DTSTrackImplTest.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/com/googlecode/mp4parser/authoring/tracks/dts-sample.mp4");
        BoxComparator.check(ref, c, "/moov[0]/mvhd[0]", "/moov[0]/trak[0]/tkhd[0]", "/moov[0]/trak[0]/mdia[0]/mdhd[0]");


    }
}