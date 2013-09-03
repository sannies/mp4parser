package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.util.Path;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class BoxReplacer {
    public static void replace(Map<String, Box> replacements, File file) throws IOException {
        final AbstractBoxParser abstractBoxParser = new PropertyBoxParserImpl();
        IsoFile isoFile = new IsoFile(new FileDataSourceImpl(file), abstractBoxParser);
        Map<String, Box> replacementSanitised = new HashMap<String, Box>();
        for (Map.Entry<String, Box> e : replacements.entrySet()) {
            Box b = Path.getPath(isoFile, e.getKey());
            replacementSanitised.put(Path.createPath(b), e.getValue());
            assert b.getSize() == e.getValue().getSize();
        }
        isoFile.close();
        FileChannel fc = new RandomAccessFile(file, "rw").getChannel();
        for (Map.Entry<String, Box> e : replacementSanitised.entrySet()) {
            String path = e.getKey();
            throw new RuntimeException("ddd");
        }
        fc.close();
    }

}
