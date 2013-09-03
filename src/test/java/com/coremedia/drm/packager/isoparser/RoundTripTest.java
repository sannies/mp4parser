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

import com.coremedia.iso.IsoFile;
import com.googlecode.mp4parser.authoring.tracks.BoxComparator;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import com.googlecode.mp4parser.DataSource;

/**
 * Tests ISO Roundtrip.
 */
public class RoundTripTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
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
        testRoundTrip_1("/Tiny Sample - OLD.mp4");
    }

    public void testRoundTrip_TinyExamples_Metaxed() throws Exception {
        testRoundTrip_1("/Tiny Sample - NEW - Metaxed.mp4");
    }

    public void testRoundTrip_TinyExamples_Untouched() throws Exception {
        testRoundTrip_1("/Tiny Sample - NEW - Untouched.mp4");
    }


    public void testRoundTrip_1a() throws Exception {
        testRoundTrip_1("/multiTrack.3gp");
    }

    public void testRoundTrip_1b() throws Exception {
        testRoundTrip_1("/MOV00006.3gp");
    }

    public void testRoundTrip_1c() throws Exception {
        testRoundTrip_1("/Beethoven - Bagatelle op.119 no.11 i.m4a");
    }

    public void testRoundTrip_1d() throws Exception {
        testRoundTrip_1("/test.m4p");
    }

    public void testRoundTrip_1e() throws Exception {
        testRoundTrip_1("/test-pod.m4a");
    }

    public void testRoundTrip_1(String resource) throws Exception {

        long start1 = System.currentTimeMillis();
        String originalFile = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile() + resource;


        long start2 = System.currentTimeMillis();

        IsoFile isoFile = new IsoFile(originalFile);

        long start3 = System.currentTimeMillis();


        long start4 = System.currentTimeMillis();
        Walk.through(isoFile);
        long start5 = System.currentTimeMillis();

        File result = File.createTempFile(this.getName(), resource.replace("/", "_"));

        FileOutputStream fos = new FileOutputStream(result);
        FileChannel fcOut = fos.getChannel();
        isoFile.getBox(fcOut);
        fcOut.close();
        fos.close();


        long start6 = System.currentTimeMillis();

     /*   System.err.println("Preparing tmp copy took: " + (start2 - start1) + "ms");
        System.err.println("Parsing took           : " + (start3 - start2) + "ms");
        System.err.println("Writing took           : " + (start6 - start3) + "ms");
        System.err.println("Walking took           : " + (start5 - start4) + "ms");*/


        IsoFile copyViaIsoFileReparsed = new IsoFile(result.getAbsolutePath());
        BoxComparator.check(isoFile, copyViaIsoFileReparsed, "/moov[0]/mvhd[0]", "/moov[0]/trak[0]/tkhd[0]", "/moov[0]/trak[0]/mdia[0]/mdhd[0]");
        isoFile.close();
        copyViaIsoFileReparsed.close();
        result.deleteOnExit();
        // as windows cannot delete file when something is memory mapped and the garbage collector
        // doesn't necessarily free the Buffers quickly enough we cannot delete the file here (we could but only for linux)


    }


    public static void copy(InputStream input, OutputStream output) throws IOException {
        assert input != null && output != null;

        byte[] buffer = new byte[4096];
        int count = input.read(buffer);
        while (count > 0) {
            output.write(buffer, 0, count);
            count = input.read(buffer);
        }
    }
}
