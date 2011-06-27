package com.coremedia.iso;

import java.io.File;
import java.io.IOException;

/**
 * Just opens a file and toString()s it.
 */
public class Test {
    public static void main(String[] args) throws IOException {
        main2(new String[]{"/home/sannies/scm/svn/mp4parser/isoparser/src/test/resources/foo1.mp4"});
        main2(new String[]{"/home/sannies/scm/svn/mp4parser/isoparser/src/test/resources/foo2.mp4"});
    }

    public static void main2(String[] args) throws IOException {
        IsoBufferWrapper isoBufferWrapper = new IsoBufferWrapperImpl(new File(args[0]));
        IsoFile isoFile = new IsoFile(isoBufferWrapper);
        isoFile.parse();
        isoFile.parseMdats();
        System.err.println(isoFile.getSize());
        System.err.println(isoFile.toString());

    }
}
