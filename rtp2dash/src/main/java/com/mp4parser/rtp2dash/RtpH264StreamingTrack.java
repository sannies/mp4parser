package com.mp4parser.rtp2dash;

import com.mp4parser.muxer.tracks.h264.H264NalUnitHeader;
import com.mp4parser.muxer.tracks.h264.H264NalUnitTypes;
import com.mp4parser.muxer.tracks.h264.H264TrackImpl;
import com.mp4parser.muxer.tracks.h264.parsing.read.BitstreamReader;
import com.mp4parser.muxer.tracks.h265.H265TrackImplOld;
import com.mp4parser.streaming.AbstractStreamingTrack;
import com.mp4parser.streaming.MultiTrackFragmentedMp4Writer;
import com.mp4parser.streaming.StreamingTrack;
import com.mp4parser.streaming.rawformats.h264.H264NalConsumingTrack;
import com.mp4parser.tools.Hex;
import com.mp4parser.tools.IsoTypeReader;
import com.mp4parser.tools.Mp4Arrays;
import sun.reflect.generics.tree.ReturnType;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * Created by sannies on 20.08.2015.
 */
public class RtpH264StreamingTrack extends H264NalConsumingTrack implements Callable<Void> {
    private static final Logger LOG = Logger.getLogger(RtpH264StreamingTrack.class.getName());
    boolean isOpen = true;
    private int initialTimeout = 10000;
    private int timeout = 1000;
    CountDownLatch countDownLatch = new CountDownLatch(1);
    private int port;



    @Override
    public boolean sourceDepleted() {
        return !isOpen;
    }

    public void close() throws IOException {
        isOpen = false;
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    public RtpH264StreamingTrack(String sprop, int port) throws IOException {
        super();
        this.port = port;
        String[] spspps = sprop.split(",");
        byte[] sps = Base64.getDecoder().decode(spspps[0]);
        consumeNal(sps);
        byte[] pps = Base64.getDecoder().decode(spspps[1]);
        consumeNal(pps);
     }

    public Void call() throws IOException {
        try {
            DatagramSocket socket = new DatagramSocket(port);
            socket.setSoTimeout(initialTimeout);
            byte[] nalBuf = new byte[0];
            byte[] buf = new byte[16384];
            LOG.info("Start Receiving H264 RTP Packets on port " + port);
            while (isOpen) {
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException e) {
                    LOG.info("Socket Timeout closed RtpH264StreamingTrack");
                    isOpen = false;
                    continue;
                }
                socket.setSoTimeout(timeout);
                BitstreamReader bsr = new BitstreamReader(new ByteArrayInputStream(packet.getData()));
                int v = (int) bsr.readNBit(2);
                boolean p = bsr.readBool();
                boolean x = bsr.readBool();
                int cc = (int) bsr.readNBit(4);
                boolean m = bsr.readBool();
                int pt = (int) bsr.readNBit(7);
                int sequenceNumber = (int) bsr.readNBit(16);
                ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 4, packet.getData().length - 4);
                bb.limit(packet.getLength());
                long ts = IsoTypeReader.readUInt32(bb);
                long ssrc = IsoTypeReader.readUInt32(bb);
                long[] csrc = new long[cc];
                for (int i = 0; i < csrc.length; i++) {
                    csrc[i] = IsoTypeReader.readUInt32(bb);
                }
                ByteBuffer payload = bb.slice();
                int nalUnitType = payload.get(0) & 0x1f;
                if (nalUnitType >= 1 && nalUnitType <= 23) {
                    byte[] nalSlice = new byte[payload.remaining()];
                    payload.get(nalSlice);
                    consumeNal(nalSlice);
                } else if (nalUnitType == 24) {
                    throw new RuntimeException("No Support for STAP A");
                } else if (nalUnitType == 25) {
                    throw new RuntimeException("No Support for STAP B");
                } else if (nalUnitType == 26) {
                    throw new RuntimeException("No Support for MTAP 16");
                } else if (nalUnitType == 27) {
                    throw new RuntimeException("No Support for MTAP 24");
                } else if (nalUnitType == 28) {
                    int fuIndicator = payload.get(0);
                    int fuHeader = IsoTypeReader.byte2int(payload.get(1));
                    boolean s = (fuHeader & 128) > 0;
                    boolean e = (fuHeader & 64) > 0;


                    //System.out.print("FU-A start: " + s + " end: " + e);
                    payload.position(2);
                    if (s) {
                        nalBuf = new byte[]{(byte) ((fuIndicator & 96) + (fuHeader & 31))};
                    }
                    if (nalBuf != null) {
                        byte[] nalSlice = new byte[payload.remaining()];
                        payload.get(nalSlice);
                        nalBuf = Mp4Arrays.copyOfAndAppend(nalBuf, nalSlice);
                    }
                    if (e && nalBuf != null) {
                        //System.out.print("FU: ");
                        consumeNal(nalBuf);
                        nalBuf = null;
                    }


                } else if (nalUnitType == 29) {
                    System.out.print("FU B ");
                    throw new RuntimeException("No Support for FU-B");
                } else {
                    throw new RuntimeException("No Support for nalUnitType " + nalUnitType);
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
            LOG.info("Done receiving RTP Packets");
            drainDecPictureBuffer(true);
            countDownLatch.countDown();
            LOG.info("Picture Buffer drained");
            return null;
        } finally {
            if (isOpen) {
                LOG.warning("Stopping RTP Receiver due to exception.");
            }
            isOpen = false;
        }
    }


    @Override
    public String toString() {
        return "RtpH264StreamingTrack{" +
                "port=" + port +
                '}';
    }
}
