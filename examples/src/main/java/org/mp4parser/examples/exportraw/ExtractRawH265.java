package org.mp4parser.examples.exportraw;

import org.mp4parser.IsoFile;
import org.mp4parser.boxes.iso14496.part12.TrackBox;
import org.mp4parser.boxes.iso14496.part15.AvcConfigurationBox;
import org.mp4parser.boxes.iso14496.part15.HevcConfigurationBox;
import org.mp4parser.boxes.iso14496.part15.HevcDecoderConfigurationRecord;
import org.mp4parser.muxer.FileRandomAccessSourceImpl;
import org.mp4parser.muxer.Sample;
import org.mp4parser.muxer.Track;
import org.mp4parser.muxer.container.mp4.Mp4SampleList;
import org.mp4parser.tools.IsoTypeReaderVariable;
import org.mp4parser.tools.Path;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;


public class ExtractRawH265 {
    public static void main(String[] args) throws IOException {
        IsoFile isoFile = new IsoFile("/Users/sannies/dev/common_transcoding-packager-encrypter/dashencrypter/15000_dv5.mp4");

        List<TrackBox> trackBoxes = Path.getPaths(isoFile, "moov/trak/");

        long trackId = 1;

        TrackBox trackBox = trackBoxes.stream().filter(tb -> tb.getTrackHeaderBox().getTrackId() == trackId).findAny().get();

        Mp4SampleList sl = new Mp4SampleList(trackId, isoFile, new FileRandomAccessSourceImpl(
                new RandomAccessFile("/Users/sannies/dev/common_transcoding-packager-encrypter/dashencrypter/15000_dv5.mp4", "r")));


        FileChannel fc = new FileOutputStream("/Users/sannies/dev/common_transcoding-packager-encrypter/dashencrypter/15000_dv5.h265").getChannel();
        ByteBuffer separator = ByteBuffer.wrap(new byte[]{0, 0, 0, 1});


        HevcConfigurationBox hevc = Path.getPath(trackBox, "mdia/minf/stbl/stsd/..../hvcC");
        List<HevcDecoderConfigurationRecord.Array> s = hevc.getArrays();


        s.stream().filter(a -> a.nal_unit_type == 32).flatMap(a -> a.nalUnits.stream()).forEach(a -> {
            try {
                fc.write((ByteBuffer) ((Buffer)separator).rewind());
                fc.write(ByteBuffer.wrap(a));
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Write VPS
        });
        s.stream().filter(a -> a.nal_unit_type == 33).flatMap(a -> a.nalUnits.stream()).forEach(a -> {
            try {
                fc.write((ByteBuffer) ((Buffer)separator).rewind());
                fc.write(ByteBuffer.wrap(a));
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Write SPS
        });
        s.stream().filter(a -> a.nal_unit_type == 34).flatMap(a -> a.nalUnits.stream()).forEach(a -> {
            try {
                fc.write((ByteBuffer) ((Buffer)separator).rewind());
                fc.write(ByteBuffer.wrap(a));
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Write PPS
        });


        for (Sample sample : sl) {
            ByteBuffer bb = sample.asByteBuffer();
            while (bb.remaining() > 0) {
                int length = (int) IsoTypeReaderVariable.read(bb, hevc.getLengthSizeMinusOne() + 1);
                fc.write((ByteBuffer) ((Buffer)separator).rewind());
                fc.write((ByteBuffer) ((Buffer)bb.slice()).limit(length));
                ((Buffer)bb).position(bb.position() + length);
            }


        }
        fc.close();

    }


}
