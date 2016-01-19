package org.mp4parser.examples.exportraw;

import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.TrackBox;
import org.mp4parser.boxes.iso14496.part15.AvcConfigurationBox;
import org.mp4parser.muxer.FileRandomAccessSourceImpl;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.samples.SampleList;
import org.mp4parser.tools.IsoTypeReaderVariable;
import org.mp4parser.tools.Path;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;


public class ExtractRawH264 {
    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile("D:\\downloads\\cracked.s01e01.hdtv.x264-2hd.mp4");

        List<TrackBox> trackBoxes = Path.getPath(isoFile, "moov/trak/");
        long trackId = -1;
        TrackBox trackBox = null;
        for (TrackBox _trackBox : trackBoxes) {
            if (Path.getPath(_trackBox, "mdia/minf/stbl/stsd/avc1") != null) {
                trackId = _trackBox.getTrackHeaderBox().getTrackId();
                trackBox = _trackBox;
            }

        }

        SampleList sl = new SampleList(trackId, isoFile, new FileRandomAccessSourceImpl(
                new RandomAccessFile("D:\\downloads\\cracked.s01e01.hdtv.x264-2hd.mp4", "r")));


        FileChannel fc = new FileOutputStream("out.h264").getChannel();
        ByteBuffer separator = ByteBuffer.wrap(new byte[]{0, 0, 0, 1});

        fc.write((ByteBuffer) separator.rewind());
        // Write SPS
        fc.write((
                ((AvcConfigurationBox) Path.getPath(trackBox, "mdia/minf/stbl/stsd/avc1/avcC")
                ).getSequenceParameterSets().get(0)));
        // Warning:
        // There might be more than one SPS (I've never seen that but it is possible)

        fc.write((ByteBuffer) separator.rewind());
        // Write PPS
        fc.write((
                ((AvcConfigurationBox) Path.getPath(trackBox, "mdia/minf/stbl/stsd/avc1/avcC")
                ).getPictureParameterSets().get(0)));
        // Warning:
        // There might be more than one PPS (I've never seen that but it is possible)

        int lengthSize = ((AvcConfigurationBox) Path.getPath(trackBox, "mdia/minf/stbl/stsd/avc1/avcC")).getLengthSizeMinusOne() + 1;
        for (Sample sample : sl) {
            ByteBuffer bb = sample.asByteBuffer();
            while (bb.remaining() > 0) {
                int length = (int) IsoTypeReaderVariable.read(bb, lengthSize);
                fc.write((ByteBuffer) separator.rewind());
                fc.write((ByteBuffer) bb.slice().limit(length));
                bb.position(bb.position() + length);
            }


        }
        fc.close();

    }


}
