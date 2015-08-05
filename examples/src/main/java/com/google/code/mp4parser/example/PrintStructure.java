package com.google.code.mp4parser.example;

import com.mp4parser.tools.IsoTypeReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: sannies
 * Date: 8/5/11
 * Time: 2:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class PrintStructure {
    public static void main(String[] args) throws IOException {
        System.out.println("PWD: " + System.getProperty("user.dir"));
        System.out.println("Input: " + args[0]);
        FileInputStream fis = new FileInputStream(new File(args[0]));
        System.out.println("input size: " + fis.getChannel().size());

        PrintStructure ps = new PrintStructure();
        ps.print(fis.getChannel(), 0, 0, 0);
    }


    /** Parses the FileChannel, in the range [start, end) and prints the elements found
     *
     * Elements are printed, indented by "level" number of spaces.  If an element is
     * a container, then its contents will be, recursively, printed, with a greater
     * indentation.
     *
     * @param fc
     * @param level
     * @param start
     * @param end
     * @throws IOException
     */
    private void print(FileChannel fc, int level, long start, long end) throws IOException {
        fc.position(start);
        if(end <= 0) {
            end = start + fc.size();
            System.out.println("Setting END to " + end);
        }
        while (end - fc.position() > 8) {
            long begin = fc.position();
            ByteBuffer bb = ByteBuffer.allocate(8);
            fc.read(bb);
            bb.rewind();
            long size = IsoTypeReader.readUInt32(bb);
            String type = IsoTypeReader.read4cc(bb);
            long fin = begin + size;
            // indent by the required number of spaces
            for (int i = 0; i < level; i++) {
                System.out.print(" ");
            }

            System.out.println(type + "@" + (begin) + " size: " + size);
            if (containers.contains(type)) {
                print(fc, level + 1, begin + 8, fin);
                if(fc.position() != fin) {
                    System.out.println("End of container contents at " + fc.position());
                    System.out.println("  FIN = " + fin);
                }
            }

            fc.position(fin);

        }
    }

    List<String> containers = Arrays.asList(
            "moov",
            "trak",
            "mdia",
            "minf",
            "udta",
            "stbl"
    );
}