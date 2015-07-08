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
    ArrayBlockingQueue<StreamingSample> samples;


    public H264TrackAdapter(final H264TrackImpl h264Track) {
        this.h264Track = h264Track;
        samples = new ArrayBlockingQueue<StreamingSample>(1000, true);

    }

    public void parse() throws InterruptedException {
        {
            long pTime = 0;
            List<Sample> oldsamples = h264Track.getSamples();

            for (int i = 0; i < oldsamples.size(); i++) {
                final long myPTime = pTime;
                final Sample sample = oldsamples.get(i);
                samples.put(new StreamingSample() {
                    public ByteBuffer getContent() {
                        return sample.asByteBuffer().duplicate();
                    }

                    public long getDuration() {
                        return myPTime;
                    }

                    public SampleExtension[] getExtensions() {
                        return new SampleExtension[0];
                    }
                });
                pTime += h264Track.getSampleDurations()[i];

            }
        }

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


    public static void main(String[] args) throws IOException {
        StreamingTrack streamingTrack = new H264TrackAdapter(new H264TrackImpl(new FileDataSourceImpl("c:\\content\\big_buck_bunny_1080p_h264-2min.h264")));
        SingleTrackFragmentedMp4Writer singleTrackFragmentedMp4Writer = new SingleTrackFragmentedMp4Writer(streamingTrack, new FileOutputStream("output.mp4"));
        singleTrackFragmentedMp4Writer.write();
    }
}
