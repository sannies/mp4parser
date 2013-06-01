package com.coremedia.iso.boxes;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sannies on 18.05.13.
 */
public class FileChannelTestHelper {
    static List<File> opened = new LinkedList<File>();

    public static FileChannel wrap(ByteBuffer bb) throws IOException {
        File f = File.createTempFile("FileChannelTestHelper", "ByteBuffer");
        opened.add(f);
        FileChannel fc = (FileChannel) Files.newByteChannel(f.toPath(), StandardOpenOption.WRITE, StandardOpenOption.READ);
        fc.write(bb);
        fc.position(0);
        return fc;
    }


    public static void cleanup() {
        for (File file : opened) {
            file.delete();
        }
        opened.clear();
    }

}
