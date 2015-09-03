import com.mp4parser.rtp2dash.RtpH264StreamingTrack;
import com.mp4parser.streaming.MultiTrackFragmentedMp4Writer;
import com.mp4parser.streaming.StreamingTrack;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.concurrent.*;
import java.util.logging.LogManager;

/**
 * Created by sannies on 23.08.2015.
 */
public class TestH264 {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        LogManager.getLogManager().readConfiguration(TestH264.class.getResourceAsStream("/log.properties"));


        RtpH264StreamingTrack st = new RtpH264StreamingTrack("Z2QAFUs2QCAb5/ARAAADAAEAAAMAMI8WLZY=,aEquJyw=", 5000);
        ExecutorService es = Executors.newCachedThreadPool();
        OutputStream os = new FileOutputStream("output.mp4");
        final MultiTrackFragmentedMp4Writer streamingMp4Writer = new MultiTrackFragmentedMp4Writer(Collections.<StreamingTrack>singletonList(st), os);
        Future<Void> stFuture = es.submit(st);
        es.submit(new Callable<Void>() {
            public Void call() throws Exception {
                streamingMp4Writer.write();
                return null;
            }
        });
        System.in.read();
        streamingMp4Writer.close();
        st.close();

        stFuture.get();
        es.shutdown();

    }
}

