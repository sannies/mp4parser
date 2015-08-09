package com.googlecode.mp4parser;

import com.mp4parser.Container;
import com.mp4parser.muxer.Movie;
import com.mp4parser.muxer.Track;
import com.mp4parser.muxer.builder.FragmentedMp4Builder;
import com.mp4parser.muxer.builder.TimeBasedFragmenter;
import com.mp4parser.muxer.container.mp4.MovieCreator;
import com.mp4parser.muxer.tracks.Amf0Track;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Properties;

/**
 * Shows a simple use of the AMF0Track
 */
public class MuxVideoWithAmf0 {
    public static void main(String[] args) throws IOException {
        String videoFile = MuxVideoWithAmf0.class.getProtectionDomain().getCodeSource().getLocation().getFile() + "/example-sans-amf0.mp4";

        Movie video = MovieCreator.build(videoFile);

        Properties props = new Properties();
        props.load(MuxVideoWithAmf0.class.getResourceAsStream("/amf0track.properties"));
        HashMap<Long, byte[]> samples = new HashMap<Long, byte[]>();
        for (String key : props.stringPropertyNames()) {
            samples.put(Long.parseLong(key), Base64.decodeBase64(props.getProperty(key)));
        }
        Track amf0Track = new Amf0Track(samples);
        amf0Track.getTrackMetaData();
        video.addTrack(amf0Track);

        FragmentedMp4Builder fragmentedMp4Builder = new FragmentedMp4Builder();
        fragmentedMp4Builder.setIntersectionFinder(new TimeBasedFragmenter(video, 2));

        Container out = fragmentedMp4Builder.build(video);
        FileOutputStream fos = new FileOutputStream(new File(String.format("output.mp4")));

        FileChannel fc = fos.getChannel();
        out.writeContainer(fc);

        fos.close();

    }
}
