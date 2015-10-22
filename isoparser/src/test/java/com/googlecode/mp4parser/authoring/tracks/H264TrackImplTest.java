/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;
import org.junit.Test;

import java.io.IOException;

/**
 * Simple test to make sure nothing breaks.
 */
public class H264TrackImplTest {

    @Test
    public void freeze() throws IOException {
        DataSource fc = new FileDataSourceImpl(getClass().getProtectionDomain().getCodeSource().getLocation().getFile() + "/com/googlecode/mp4parser/authoring/tracks/h264-sample.h264");
        H264TrackImpl.BUFFER = 65535; // make sure we are not just in one buffer
        Track t = new H264TrackImpl(fc);
        Movie m = new Movie();
        m.addTrack(t);

        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        Container c = mp4Builder.build(m);

        //c.writeContainer(new FileOutputStream("C:\\dev\\mp4parser\\isoparser\\src\\test\\resources\\com\\googlecode\\mp4parser\\authoring\\tracks\\h264-sample.mp4").getChannel());


        IsoFile isoFileReference = new IsoFile(getClass().getProtectionDomain().getCodeSource().getLocation().getFile() + "com/googlecode/mp4parser/authoring/tracks/h264-sample.mp4");
        BoxComparator.check(c, isoFileReference, "/ftyp", "/moov[0]/mvhd[0]", "/moov[0]/trak[0]/tkhd[0]", "/moov[0]/trak[0]/mdia[0]/mdhd[0]", "/moov[0]/trak[0]/mdia[0]/minf[0]/stbl[0]/stco[0]");
    }
}
