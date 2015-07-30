package com.mp4parser.streaming.rawformats;

import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.googlecode.mp4parser.FileDataSourceImpl;
import com.googlecode.mp4parser.authoring.Sample;
import com.googlecode.mp4parser.authoring.tracks.h264.H264TrackImpl;
import com.mp4parser.streaming.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by sannies on 25.05.2015.
 */
public class H264TrackAdapter extends AbstractStreamingTrack {

    H264TrackImpl h264Track;


    public H264TrackAdapter(final H264TrackImpl h264Track) throws InterruptedException {
        this.h264Track = h264Track;
        samples = new ArrayBlockingQueue<StreamingSample>(100, true);
        new Thread() {
            @Override
            public void run() {
                try {
                    parse();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        stsd = h264Track.getSampleDescriptionBox();
    }

    public void parse() throws InterruptedException {

        List<Sample> oldsamples = h264Track.getSamples();

        for (int i = 0; i < oldsamples.size(); i++) {
            System.err.println("Jo! " + i + " of " + oldsamples.size());
            final long duration = h264Track.getSampleDurations()[i];
            final Sample sample = oldsamples.get(i);

            samples.put(new StreamingSample() {
                public ByteBuffer getContent() {
                    return sample.asByteBuffer().duplicate();
                }

                public long getDuration() {
                    return duration;
                }

                public SampleExtension[] getExtensions() {
                    return new SampleExtension[0];
                }
            });

        }
        System.err.println("Jo!");

    }

    public long getTimescale() {
        return h264Track.getTrackMetaData().getTimescale();
    }

    public String getHandler() {
        return h264Track.getHandler();
    }

    public String getLanguage() {
        return h264Track.getTrackMetaData().getLanguage();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        H264TrackImpl h264Track = new H264TrackImpl(new FileDataSourceImpl("c:\\content\\big_buck_bunny_1080p_h264-2min.h264"));
        final StreamingTrack streamingTrack = new H264TrackAdapter(h264Track);
        MultiTrackFragmentedMp4Writer mp4Writer
                = new MultiTrackFragmentedMp4Writer(new StreamingTrack[]{streamingTrack}, new FileOutputStream("output.mp4"));
        mp4Writer.write();
    }
}
