package com.googlecode.mp4parser.stuff;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.XmlBox;
import com.googlecode.mp4parser.DataSource;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.util.Path;

import java.io.FileInputStream;
import java.io.IOException;

/**
 *
 */
public class ReadExample {
    public static void main(String[] args) throws IOException {
        DataSource ds = new FileDataSourceImpl("/home/sannies2/Mission_Impossible_Ghost_Protocol_Feature_SDUV_480p_16avg192max.uvu");
        IsoFile isoFile = new IsoFile(ds);
        //isoFile = new IsoFile(Channels.newChannel(new FileInputStream(this.filePath)));
        //Path path = new Path(isoFile);
        XmlBox xmlBox = (XmlBox) Path.getPath(isoFile, "/moov/meta/xml ");
        String xml = xmlBox.getXml();
        System.err.println(xml);


    }


}
