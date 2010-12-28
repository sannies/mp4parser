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
import com.coremedia.iso.boxes.MediaDataBox;
import junit.framework.TestCase;
import junitx.framework.ArrayAssert;

import javax.tools.FileObject;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    public void testRoundTrip_1(String resource) throws Exception {

        File originalFile = File.createTempFile("pdcf", "original");
        FileOutputStream fos = new FileOutputStream(originalFile);
        byte[] content = read(getClass().getResourceAsStream(resource));
        fos.write(content);
        fos.close();

        IsoFile isoFile = new IsoFile(InputStreamIsoBufferHelper.get(getClass().getResourceAsStream(resource), 20000));
        isoFile.parse();
        Walk.through(isoFile);
        isoFile.parseMdats();
		isoFile.switchToAutomaticChunkOffsetBox();
		isoFile.getBoxes(MediaDataBox.class)[0].getSample(0).toString();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        isoFile.write(baos);

        new FileOutputStream("/home/sannies/a").write(baos.toByteArray());

        ArrayAssert.assertEquals(content, baos.toByteArray());

    }

    private byte[] read(InputStream resourceAsStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(resourceAsStream, baos);
        return baos.toByteArray();
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
