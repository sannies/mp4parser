package com.coremedia.iso;

import java.io.File;
import java.io.IOException;

/**
 * Just opens a file and toString()s it.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        IsoBufferWrapper isoBufferWrapper = new IsoBufferWrapper(new File(args[0]));
        IsoFile isoFile = new IsoFile(isoBufferWrapper);
        isoFile.parse();
        isoFile.parseMdats();
        System.err.println(isoFile.getSize());
        System.err.println(isoFile.toString());

    }
}
