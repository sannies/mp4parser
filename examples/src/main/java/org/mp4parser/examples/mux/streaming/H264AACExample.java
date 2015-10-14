package org.mp4parser.examples.mux.streaming;

import org.mp4parser.streaming.MultiTrackFragmentedMp4Writer;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.rawformats.aac.AdtsAacStreamingTrack;
import org.mp4parser.streaming.rawformats.h264.H264AnnexBTrack;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;


public class H264AACExample {
    public static void main(String[] args) throws Exception {
  /*      AdtsAacStreamingTrack aac = new AdtsAacStreamingTrack(


                new URI("http://org.mp4parser.s3.amazonaws.com/examples/Cosmos%20Laundromat%20small.aac").
                        toURL().openStream(), 48000, 64000); // How should I know avg bitrate in advance?
        H264AnnexBTrack h264 = new H264AnnexBTrack(
                new URI("http://org.mp4parser.s3.amazonaws.com/examples/Cosmos%20Laundromat%20small.264").
                        toURL().openStream());*/
        AdtsAacStreamingTrack aac = new AdtsAacStreamingTrack(
                new FileInputStream("c:\\dev\\mp4parser\\843D111F-E839-4597-B60C-3B8114E0AA72_AU01.aac"), 48000, 64000); // How should I know avg bitrate in advance?
        H264AnnexBTrack h264 = new H264AnnexBTrack(
                new FileInputStream("c:\\dev\\mp4parser\\843D111F-E839-4597-B60C-3B8114E0AA72_ABR01.h264"));

        ExecutorService es = Executors.newCachedThreadPool();
        CompletionService<Void> ecs
                = new ExecutorCompletionService<>(es);

        FileOutputStream fos = new FileOutputStream("c:\\dev\\mp4parser\\output.mp4");
        WritableByteChannel wbc = fos.getChannel();
        MultiTrackFragmentedMp4Writer mtfmw = new MultiTrackFragmentedMp4Writer(Arrays.<StreamingTrack>asList(h264, aac), wbc);


        final List<Future<Void>> allFutures = new ArrayList<>();
        List<Callable<Void>> allCallables = new ArrayList<>();
        allCallables.add(aac);
        allCallables.add(h264);


        allCallables.forEach(callable -> allFutures.add(ecs.submit(callable)));
        System.out.println("Reading and writing started.");
        while (true) {
            allFutures.removeIf(Future::isDone);
            if (!allFutures.isEmpty()) {
                try {
                    ecs.take().get();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(e);
                } catch (ExecutionException e) {
                    System.out.println("Execution exception " + e.getMessage());
                    e.printStackTrace();
                    for (Future<Void> future : allFutures) {
                        if (!future.isDone()) {
                            System.out.println("Cancelling " + future);
                            future.cancel(true);
                        }
                    }
                    break;
                }
            } else {
                break;
            }
        }
        mtfmw.close(); // writes the remaining samples
        wbc.close(); // close file
        es.shutdown();
    }

}
