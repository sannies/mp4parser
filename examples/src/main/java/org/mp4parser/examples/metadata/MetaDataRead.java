package org.mp4parser.examples.metadata;


import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.XmlBox;
import org.mp4parser.tools.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Change metadata and make sure chunkoffsets are corrected.
 */
public class MetaDataRead {


    public static void main(String[] args) throws IOException {
        MetaDataRead cmd = new MetaDataRead();
        String xml = cmd.read("c:\\content\\pbs_sba101d_browser_multi.wvm_0.mp4");
        System.err.println(xml);
    }

    public String read(String videoFilePath) throws IOException {

        File videoFile = new File(videoFilePath);
        if (!videoFile.exists()) {
            throw new FileNotFoundException("File " + videoFilePath + " not exists");
        }

        if (!videoFile.canRead()) {
            throw new IllegalStateException("No read permissions to file " + videoFilePath);
        }
        IsoFile isoFile = new IsoFile(videoFilePath);
        XmlBox xmlBox = Path.getPath(isoFile, "moov[0]/udta[0]/meta[0]/xml [0]");
        String xml = xmlBox.getXml();
        isoFile.close();
        return xml;
    }
}
