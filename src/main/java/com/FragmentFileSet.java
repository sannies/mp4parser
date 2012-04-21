package com;


import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.smoothstreaming.FlatPackageWriterImpl;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.Channels;

public class FragmentFileSet {
    /*
    static String[] inputs = new String[]{
            "/home/sannies/scm/svn/mp4parser/isoparser/src/test/resources/BBB_qpfile_10sec/BBB_fixedres_B_180x320_80.mp4",
            "/home/sannies/scm/svn/mp4parser/isoparser/src/test/resources/BBB_qpfile_10sec/BBB_fixedres_B_180x320_100.mp4",
            "/home/sannies/scm/svn/mp4parser/isoparser/src/test/resources/BBB_qpfile_10sec/BBB_fixedres_B_180x320_120.mp4",
            "/home/sannies/scm/svn/mp4parser/isoparser/src/test/resources/BBB_qpfile_10sec/BBB_fixedres_B_180x320_150.mp4",
            "/home/sannies/scm/svn/mp4parser/isoparser/src/test/resources/BBB_qpfile_10sec/BBB_fixedres_B_180x320_200.mp4"
    };  */
    /* static String[] inputs = new String[]{
            "/home/sannies/scm/svn/mp4parser/examples/src/main/resources/smoothstreaming/audio-96000.mp4",
            "/home/sannies/scm/svn/mp4parser/examples/src/main/resources/smoothstreaming/video-128h-75kbps.mp4",
            "/home/sannies/scm/svn/mp4parser/examples/src/main/resources/smoothstreaming/video-192h-155kbps.mp4",
            "/home/sannies/scm/svn/mp4parser/examples/src/main/resources/smoothstreaming/video-240h-231kbps.mp4",
            "/home/sannies/scm/svn/mp4parser/examples/src/main/resources/smoothstreaming/video-320h-388kbps.mp4"
    };   */

    static String[] inputs = new String[]{
            "/home/sannies/scm/svn/drmtoday-trunk/fragmencrypter/src/test/resources/IAC_fixedres0_B_135x240_150.mp4",
            "/home/sannies/scm/svn/drmtoday-trunk/fragmencrypter/src/test/resources/IAC_fixedres1_B_180x320_150.mp4",

            "/home/sannies/scm/svn/drmtoday-trunk/fragmencrypter/src/test/resources/IAC_fixedres2_B_135x240_150.mp4",
            "/home/sannies/scm/svn/drmtoday-trunk/fragmencrypter/src/test/resources/IAC_fixedres3_B_180x320_150.mp4",

            "/home/sannies/scm/svn/drmtoday-trunk/fragmencrypter/src/test/resources/IAC_fixedres4_B_135x240_150.mp4",
            "/home/sannies/scm/svn/drmtoday-trunk/fragmencrypter/src/test/resources/IAC_fixedres5_B_180x320_150.mp4",

            "/home/sannies/scm/svn/drmtoday-trunk/fragmencrypter/src/test/resources/RIO_24000_mono.wav.24000Hz.32k.LC.1chan.mp4",
            "/home/sannies/scm/svn/drmtoday-trunk/fragmencrypter/src/test/resources/RIO_24000_stereo.wav.24000Hz.128k.LC.2chan.mp4",
            "/home/sannies/scm/svn/drmtoday-trunk/fragmencrypter/src/test/resources/RIO_48000_5.1.wav.48000Hz.1024k.LC.6chan.mp4",
            "/home/sannies/scm/svn/drmtoday-trunk/fragmencrypter/src/test/resources/RIO_48000_stereo.wav.48000Hz.128k.LC.2chan.mp4",
    };

    public static void main(String[] args) throws IOException {
        FlatPackageWriterImpl flatPackageWriter = new FlatPackageWriterImpl();
        flatPackageWriter.setOutputDirectory(new File("."));
        flatPackageWriter.setWriteSingleFile(true);
        Movie movie = new Movie();
        for (String input : inputs) {
            Movie m = MovieCreator.build(Channels.newChannel(new FileInputStream(input)));
            for (Track track : m.getTracks()) {
                movie.addTrack(track);
            }

        }

        flatPackageWriter.write(movie);
    }
}
