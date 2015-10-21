package org.mp4parser.examples.mux.streaming;

import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.input.aac.AdtsAacStreamingTrack;
import org.mp4parser.streaming.input.h264.H264AnnexBTrack;
import org.mp4parser.streaming.output.mp4.FragmentedMp4Writer;

import java.io.FileOutputStream;
import java.net.URI;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.logging.LogManager;


public class H264AACExample {
    public static void main(String[] args) throws Exception {
        LogManager.getLogManager().readConfiguration(H264AACExample.class.getResourceAsStream("/log.properties"));
        AdtsAacStreamingTrack aac = new AdtsAacStreamingTrack(
                new URI("http://org.mp4parser.s3.amazonaws.com/examples/Cosmos%20Laundromat%20small.aac").
                        toURL().openStream(), 48000, 64000); // How should I know avg bitrate in advance?
        H264AnnexBTrack h264 = new H264AnnexBTrack(
                new URI("http://org.mp4parser.s3.amazonaws.com/examples/Cosmos%20Laundromat%20small.264").
                        toURL().openStream());
        /*InputStream aacInputStream = new FileInputStream("c:\\dev\\mp4parser\\843D111F-E839-4597-B60C-3B8114E0AA72_AU01.aac");
        AdtsAacStreamingTrack aac = new AdtsAacStreamingTrack(
                aacInputStream, 48000, 64000); // How should I know avg bitrate in advance?
        InputStream h264InputStream = new FileInputStream("c:\\dev\\mp4parser\\843D111F-E839-4597-B60C-3B8114E0AA72_ABR05.h264");
        H264AnnexBTrack h264 = new H264AnnexBTrack(h264InputStream);
*/
        ExecutorService es = Executors.newCachedThreadPool();
        CompletionService<Void> ecs
                = new ExecutorCompletionService<>(es);

        FileOutputStream fos = new FileOutputStream("c:\\dev\\mp4parser\\output.mp4");
        WritableByteChannel wbc = fos.getChannel();
        //AsyncWritableByteChannel asyncWritableByteChannel = new AsyncWritableByteChannel(wbc);
        FragmentedMp4Writer multiTrackFragmentedMp4Writer =
                new FragmentedMp4Writer(Arrays.<StreamingTrack>asList(aac, h264), wbc);


        final List<Future<Void>> allFutures = new ArrayList<>();
        List<Callable<Void>> allCallables = new ArrayList<>();
        allCallables.add(aac);
        allCallables.add(h264);

        for (Callable<Void> callable : allCallables) {
            allFutures.add(ecs.submit(callable));
        }

        System.out.println("Reading and writing started.");
        while (true) {
            List<Future<Void>> toBeRemoved = new ArrayList<>();
            for (Future<Void> future : allFutures) {
                if (future.isDone()) {
                    toBeRemoved.add(future);
                }
            }
            allFutures.removeAll(toBeRemoved);


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
        //asyncWritableByteChannel.close();
        multiTrackFragmentedMp4Writer.close(); // writes the remaining samples

        fos.close();
        es.shutdown();
    }

}
