/*  
 * Copyright 2008 CoreMedia AG, Hamburg
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

package com.coremedia.drm.packager.isoparser;

import junit.framework.TestCase;
import org.mp4parser.IsoFile;
import org.mp4parser.support.BoxComparator;
import org.mp4parser.tools.ByteBufferByteChannel;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.nio.channels.Channels;

/**
 * Tests ISO Roundtrip.
 */
public class RoundTripTest extends TestCase {
    String defaultTestFileDir;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        defaultTestFileDir = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();
/*        Logger.getLogger("").setLevel(Level.ALL);
    Handler[] handlers = Logger.getLogger("").getHandlers();
    for (Handler handler : handlers) {
      handler.setLevel(Level.ALL);
    }*/
    }

    /*    public void testRoundDeleteMe() throws Exception {
        testRoundTrip_1("/suckerpunch-distantplanet_h1080p.mov");
    }*/
    public void testRoundTrip_TinyExamples_Old() throws Exception {
        testRoundTrip_1(defaultTestFileDir + "/Tiny Sample - OLD.mp4");
    }

    public void testRoundTrip_TinyExamples_Metaxed() throws Exception {
        testRoundTrip_1(defaultTestFileDir + "/Tiny Sample - NEW - Metaxed.mp4");
    }

    public void testRoundTrip_TinyExamples_Untouched() throws Exception {
        testRoundTrip_1(defaultTestFileDir + "/Tiny Sample - NEW - Untouched.mp4");
    }


    public void testRoundTrip_1a() throws Exception {
        testRoundTrip_1(defaultTestFileDir + "/multiTrack.3gp");
    }

    public void testRoundTrip_1b() throws Exception {
        testRoundTrip_1(defaultTestFileDir + "/MOV00006.3gp");
    }

    public void testRoundTrip_1c() throws Exception {
        testRoundTrip_1(defaultTestFileDir + "/Beethoven - Bagatelle op.119 no.11 i.m4a");
    }

    public void testRoundTrip_1d() throws Exception {
        testRoundTrip_1(defaultTestFileDir + "/test.m4p");
    }

    public void testRoundTrip_1e() throws Exception {
        testRoundTrip_1(defaultTestFileDir + "/test-pod.m4a");
    }


    public void testRoundTrip_1(String originalFile) throws Exception {

        long start1 = System.currentTimeMillis();
        long start2 = System.currentTimeMillis();

        IsoFile isoFile = new IsoFile(new FileInputStream(originalFile).getChannel());

        long start3 = System.currentTimeMillis();


        long start4 = System.currentTimeMillis();
        Walk.through(isoFile);
        long start5 = System.currentTimeMillis();


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        isoFile.getBox(Channels.newChannel(baos));


        long start6 = System.currentTimeMillis();

     /*   System.err.println("Preparing tmp copy took: " + (start2 - start1) + "ms");
        System.err.println("Parsing took           : " + (start3 - start2) + "ms");
        System.err.println("Writing took           : " + (start6 - start3) + "ms");
        System.err.println("Walking took           : " + (start5 - start4) + "ms");*/


        IsoFile copyViaIsoFileReparsed = new IsoFile(new ByteBufferByteChannel(baos.toByteArray()));
        BoxComparator.check(isoFile, copyViaIsoFileReparsed, "moov[0]/mvhd[0]", "moov[0]/trak[0]/tkhd[0]", "moov[0]/trak[0]/mdia[0]/mdhd[0]");
        isoFile.close();
        copyViaIsoFileReparsed.close();
        // as windows cannot delete file when something is memory mapped and the garbage collector
        // doesn't necessarily free the Buffers quickly enough we cannot delete the file here (we could but only for linux)


    }


}
