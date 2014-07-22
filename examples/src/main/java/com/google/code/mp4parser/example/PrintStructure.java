package com.google.code.mp4parser.example;

import com.coremedia.iso.IsoTypeReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import com.googlecode.mp4parser.DataSource;

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
        FileInputStream fis = new FileInputStream(new File(args[0]));

        PrintStructure ps = new PrintStructure();
        ps.print(fis.getChannel(), 0, 0);
    }


    private void print(FileChannel fc, int level, long baseoffset) throws IOException {

        while (fc.size() - fc.position() > 8) {
            long start = fc.position();
            ByteBuffer bb = ByteBuffer.allocate(8);
            fc.read(bb);
            bb.rewind();
            long size = IsoTypeReader.readUInt32(bb);
            String type = IsoTypeReader.read4cc(bb);
            long end = start + size;
            for (int i = 0; i < level; i++) {
                System.out.print(" ");
            }

            System.out.println(type + "@" + (baseoffset + start) + " size: " + size);
            if (containers.contains(type)) {
                print(fc, level + 1, baseoffset + start + 8);
            }

            fc.position(end);

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
