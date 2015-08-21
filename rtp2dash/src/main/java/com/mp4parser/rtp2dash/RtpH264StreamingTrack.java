package com.mp4parser.rtp2dash;

import com.mp4parser.muxer.tracks.h264.H264NalUnitHeader;
import com.mp4parser.muxer.tracks.h264.H264NalUnitTypes;
import com.mp4parser.muxer.tracks.h264.H264TrackImpl;
import com.mp4parser.muxer.tracks.h264.parsing.read.BitstreamReader;
import com.mp4parser.muxer.tracks.h265.H265TrackImplOld;
import com.mp4parser.streaming.AbstractStreamingTrack;
import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.Mp4Arrays;
import sun.reflect.generics.tree.ReturnType;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * Created by sannies on 20.08.2015.
 */
public class RtpH264StreamingTrack extends AbstractStreamingTrack {


    public RtpH264StreamingTrack(String sps ) {
    }

    public static void main(String[] args) throws IOException {
        DatagramSocket socket = new DatagramSocket(5000);
        byte[] nalBuf = new byte[0];
        byte[] buf = new byte[16384];
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);

            BitstreamReader bsr = new BitstreamReader(new ByteArrayInputStream(packet.getData()));
            int v = (int) bsr.readNBit(2);
            boolean p = bsr.readBool();
            boolean x = bsr.readBool();
            int cc = (int) bsr.readNBit(4);
            boolean m = bsr.readBool();
            int pt = (int) bsr.readNBit(7);
            int sequenceNumber = (int) bsr.readNBit(16);
            ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 4, packet.getData().length - 4);
            bb.limit(packet.getLength() - 4);
            long ts = IsoTypeReader.readUInt32(bb);
            long ssrc = IsoTypeReader.readUInt32(bb);
            long[] csrc = new long[cc];
            for (int i = 0; i < csrc.length; i++) {
                csrc[i] = IsoTypeReader.readUInt32(bb);
            }
            ByteBuffer payload = bb.slice();
            int nalUnitType = payload.get(0) & 0x1f;
            if (nalUnitType >= 1 && nalUnitType <= 23) {

                consumeNal(payload);
//                System.out.print("Single NAL Packet ");
            } else if (nalUnitType == 24) {
                //              System.out.print("STAP A ");
            } else if (nalUnitType == 25) {
                //            System.out.print("STAP B ");
            } else if (nalUnitType == 26) {
                //          System.out.print("MTAP 16 ");
            } else if (nalUnitType == 27) {
                //        System.out.print("MTAP 24 ");
            } else if (nalUnitType == 28) {
                int fuIndicator = payload.get(0);
                int fuHeader = IsoTypeReader.byte2int(payload.get(1));
                boolean s = (fuHeader & 128) > 0;
                boolean e = (fuHeader & 64) > 0;


                //System.out.print("FU-A start: " + s + " end: " + e);
                payload.position(2);
                if (s) {
                    nalBuf = new byte[] {(byte) ((fuIndicator & 96) + (fuHeader & 31))};
                }
                if (nalBuf != null) {
                    byte[] nalSlice = new byte[payload.remaining()];
                    payload.get(nalSlice);
                    nalBuf = Mp4Arrays.copyOfAndAppend(nalBuf, nalSlice);
                }
                if (e && nalBuf != null) {
                    System.out.print("FU: ");
                    consumeNal(ByteBuffer.wrap(nalBuf));
                    nalBuf = null;
                }


            } else if (nalUnitType == 29) {
                System.out.print("FU B ");
            } else {
                throw new RuntimeException("jsdhfjkshjkld");
            }

            String log = "Receiver{" +
                    "v=" + v +
                    ", len=" + packet.getLength() +
                    ", p=" + p +
                    ", x=" + x +
                    ", cc=" + cc +
                    ", m=" + m +
                    ", pt=" + pt +
                    ", sequenceNumber=" + sequenceNumber +
                    ", ts=" + ts +
                    ", ssrc=" + ssrc;
            for (int i = 0; i < csrc.length; i++) {
                log += ", csrc[" + i + "]=" + csrc[i];

            }
            log += '}';

            //System.out.println(log);


        }

    }

    public static void consumeNal(ByteBuffer nal) {
        H264NalUnitHeader nuh = H264TrackImpl.getNalUnitHeader(nal);
        for (Field f : H264NalUnitTypes.class.getFields()) {
            try {
                if (nuh.nal_unit_type == f.getInt(null)) {
                    System.out.println(f.getName() + "[" + nuh.nal_unit_type + "]");
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException();
            }
        }


    }
}
