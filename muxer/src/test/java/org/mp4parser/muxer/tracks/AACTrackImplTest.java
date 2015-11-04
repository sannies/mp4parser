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
package org.mp4parser.muxer.tracks;

import org.junit.Test;
import org.mp4parser.Container;
import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part14.ESDescriptorBox;
import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.Movie;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.builder.DefaultMp4Builder;
import org.mp4parser.support.BoxComparator;
import org.mp4parser.tools.Hex;
import org.mp4parser.tools.Path;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;

/**
 * Simple test to make sure nothing breaks.
 */
public class AACTrackImplTest {


    public static void main(String[] args) throws IOException {
        ESDescriptorBox esds = Path.getPath(new IsoFile("C:\\dev\\mp4parer\\aac-sample.mp4"), "/moov[0]/trak[0]/mdia[0]/minf[0]/stbl[0]/stsd[0]/mp4v[0]/esds[0]");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        esds.getBox(Channels.newChannel(baos));
        System.err.println(Hex.encodeHex(baos.toByteArray()));
        System.err.println(esds.getEsDescriptor());
        baos = new ByteArrayOutputStream();
        esds.getBox(Channels.newChannel(baos));
        System.err.println(Hex.encodeHex(baos.toByteArray()));
    }

    @Test
    public void freeze() throws IOException {
        Track t = new AACTrackImpl(new FileDataSourceImpl(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile() + "/org/mp4parser/muxer/tracks/aac-sample.aac"));
        //Track t = new AACTrackImpl2(new FileInputStream(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile() + "/com/googlecode/mp4parser/authoring/tracks/aac-sample.aac"));
        Movie m = new Movie();
        m.addTrack(t);

        DefaultMp4Builder mp4Builder = new DefaultMp4Builder();
        Container c = mp4Builder.build(m);
        //c.writeContainer(new FileOutputStream("C:\\dev\\mp4parser\\isoparser\\src\\test\\resources\\com\\googlecode\\mp4parser\\authoring\\tracks\\aac-sample.mp4").getChannel());

        IsoFile isoFileReference = new IsoFile(this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile() + "/org/mp4parser/muxer/tracks/aac-sample.mp4");
        BoxComparator.check(c, isoFileReference, "moov[0]/mvhd[0]", "moov[0]/trak[0]/tkhd[0]", "moov[0]/trak[0]/mdia[0]/mdhd[0]", "moov[0]/trak[0]/mdia[0]/minf[0]/stbl[0]/stco[0]");
    }
}
