package com.googlecode.mp4parser;

import org.mp4parser.muxer.FileDataSourceImpl;
import org.mp4parser.muxer.tracks.AbstractH26XTrack;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by sannies on 11.02.2015.
 */
public class ParseMdatByHand {
    static ByteBuffer findNextNal(AbstractH26XTrack.LookAhead la) throws IOException {
        try {
            while (!la.nextThreeEquals001()) {
                la.discardByte();
            }
            la.discardNext3AndMarkStart();

            while (!la.nextThreeEquals000or001orEof(true)) {
                la.discardByte();
            }
            return la.getNal();
        } catch (EOFException e) {
            return null;
        }
    }


    public static void main(String[] args) throws IOException {
        File dir = new File("C:\\dev\\ASAN\\record");
        File[] h264s = dir.listFiles();
        Arrays.sort(h264s);
        int i = 0;
        for (File h264 : h264s) {
            AbstractH26XTrack.LookAhead lookAhead = new AbstractH26XTrack.LookAhead(new FileDataSourceImpl(h264));
            ByteBuffer nal = null;

            while ((nal = findNextNal(lookAhead))!=null) {
                int type = nal.get(0);
                int nal_ref_idc = (type >> 5) & 3;
                int nal_unit_type = type & 0x1f;
                //System.err.println(nal_unit_type);
                if (nal_unit_type == 7) {
                    byte[] nalArray = new byte[nal.remaining()];
                    nal.get(nalArray);
                    //SeqParameterSet sps = SeqParameterSet.read(new CleanInputStream(new ByteArrayInputStream(nalArray)));
                    System.err.println(i++);
                } else {
                    i++;
                }
            }
            //System.err.println("--------------");
        }
    }
}
