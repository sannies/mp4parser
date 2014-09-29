package com.google.code.mp4parser.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;


public class GetHeight {
    byte[] lastTkhd;
    List<String> containers = Arrays.asList(
            "moov",
            "mdia",
            "trak"
    );

    public static void main(String[] args) throws IOException {
        FileInputStream fis = new FileInputStream(new File(args[0]));

        GetHeight ps = new GetHeight();
        ps.find(fis);
    }

    public static long readUint32(byte[] b, int s) {
        long result = 0;
        result |= ((b[s + 0] << 24) & 0xFF000000);
        result |= ((b[s + 1] << 16) & 0xFF0000);
        result |= ((b[s + 2] << 8) & 0xFF00);
        result |= ((b[s + 3]) & 0xFF);
        return result;
    }

    public static double readFixedPoint1616(byte[] b, int s) {
        return ((double) readUint32(b, s)) / 65536;
    }

    private void find(InputStream fis) throws IOException {

        while (fis.available() > 0) {
            byte[] header = new byte[8];
            fis.read(header);

            long size = readUint32(header, 0);
            String type = new String(header, 4, 4, "ISO-8859-1");
            if (containers.contains(type)) {
                find(fis);
            } else {
                if (type.equals("tkhd")) {
                    lastTkhd = new byte[(int) (size - 8)];
                    fis.read(lastTkhd);
                } else {
                    if (type.equals("hdlr")) {
                        byte[] hdlr = new byte[(int) (size - 8)];
                        fis.read(hdlr);
                        if (hdlr[8] == 0x76 && hdlr[9] == 0x69 && hdlr[10] == 0x64 && hdlr[11] == 0x65) {
                            System.out.println("Video Track Header identified");
                            System.out.println("width: " + readFixedPoint1616(lastTkhd, lastTkhd.length - 8));
                            System.out.println("height: " + readFixedPoint1616(lastTkhd, lastTkhd.length - 4));
                            System.exit(1);
                        }
                    } else {
                        fis.skip(size - 8);
                    }
                }
            }
        }
    }
}
