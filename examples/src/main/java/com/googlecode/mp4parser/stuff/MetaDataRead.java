package com.googlecode.mp4parser.stuff;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.*;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.util.Path;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.List;

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
        IsoFile isoFile = new IsoFile(new FileDataSourceImpl(videoFilePath));
        XmlBox xmlBox = Path.getPath(isoFile, "/moov[0]/udta[0]/meta[0]/xml [0]");
        String xml = xmlBox.getXml();
        isoFile.close();
        return xml;
    }
}
