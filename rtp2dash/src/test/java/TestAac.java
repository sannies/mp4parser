import com.mp4parser.rtp2dash.RtpAacStreamingTrack;
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
 * Created by sannies on 03.09.2015.
 */
public class TestAac {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        LogManager.getLogManager().readConfiguration(TestAac.class.getResourceAsStream("/log.properties"));


        RtpAacStreamingTrack st = new RtpAacStreamingTrack(5005, 97, 128, "profile-level-id=1;mode=AAC-hbr;sizelength=13;indexlength=3;indexdeltalength=3; config=1190", "MPEG4-GENERIC/48000/2");
        ExecutorService es = Executors.newCachedThreadPool();
        OutputStream os = new FileOutputStream("output.mp4");
        final MultiTrackFragmentedMp4Writer streamingMp4Writer = new MultiTrackFragmentedMp4Writer(Collections.<StreamingTrack>singletonList(st), os);
        Future<Void> stFuture = es.submit(st);
        streamingMp4Writer.write();
        stFuture.get();
        streamingMp4Writer.close();
        st.close();


        es.shutdown();

    }
}
