package com.googlecode.mp4parser.stuff;

import com.mp4parser.IsoFile;
import com.mp4parser.boxes.iso14496.part12.XmlBox;
import com.mp4parser.tools.Path;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

/**
 *
 */
public class ReadExample {
    public static void main(String[] args) throws IOException {
        FileChannel channel = new FileInputStream("/home/sannies2/Mission_Impossible_Ghost_Protocol_Feature_SDUV_480p_16avg192max.uvu").getChannel();
        IsoFile isoFile = new IsoFile(channel);
        //isoFile = new IsoFile(Channels.newChannel(new FileInputStream(this.filePath)));
        //Path path = new Path(isoFile);
        XmlBox xmlBox = Path.getPath(isoFile, "/moov/meta/xml ");
        assert xmlBox != null;
        String xml = xmlBox.getXml();
        System.err.println(xml);


    }


}
