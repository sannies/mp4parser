/*
 * Copyright 2012 Sebastian Annies, Hamburg
 *
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.googlecode.mp4parser.authoring.builder.smoothstreaming;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.Mp4Builder;
import com.googlecode.mp4parser.authoring.builder.SyncSampleIntersectFinderImpl;
import com.googlecode.mp4parser.authoring.tracks.ChangeTimeScaleTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import static com.googlecode.mp4parser.util.CastUtils.l2i;
import static com.googlecode.mp4parser.util.Math.gcd;
import static com.googlecode.mp4parser.util.Math.lcm;

public class FlatPackageWriterImpl implements PackageWriter {
    private static Logger LOG = Logger.getLogger(FlatPackageWriterImpl.class.getName());


    private File outputDirectory;
    private boolean writeSingleFile;
    private Mp4Builder ismvBuilder;
    ManifestWriter manifestWriter;

    long timeScale = 10000000;


    {
        ismvBuilder = new FragmentedMp4Builder();
        ((FragmentedMp4Builder) ismvBuilder).setIntersectionFinder(new SyncSampleIntersectFinderImpl());
        manifestWriter = new FlatManifestWriterImpl();
    }


    public void setOutputDirectory(File outputDirectory) {
        assert outputDirectory.isDirectory();
        this.outputDirectory = outputDirectory;

    }

    public void setWriteSingleFile(boolean writeSingleFile) {
        this.writeSingleFile = writeSingleFile;
    }

    public void setIsmvBuilder(Mp4Builder ismvBuilder) {
        this.ismvBuilder = ismvBuilder;
    }

    public void setManifestWriter(ManifestWriter manifestWriter) {
        this.manifestWriter = manifestWriter;
    }

    /**
     * Writes the movie given as <code>qualities</code> flattened into the
     * <code>outputDirectory</code>.
     *
     * @param qualities
     * @throws IOException
     */
    public void write(Movie qualities) throws IOException {

        if (writeSingleFile) {
            DefaultMp4Builder defaultMp4Builder = new DefaultMp4Builder();
            IsoFile muxed = defaultMp4Builder.build(qualities);
            File muxedFile = new File(outputDirectory, "debug_1_muxed.mp4");
            FileOutputStream muxedFileOutputStream = new FileOutputStream(muxedFile);
            muxed.getBox(muxedFileOutputStream.getChannel());
            muxedFileOutputStream.close();
        }

        qualities = correctTimescale(qualities);
        if (writeSingleFile) {
            DefaultMp4Builder defaultMp4Builder = new DefaultMp4Builder();
            IsoFile muxed = defaultMp4Builder.build(qualities);
            File muxedFile = new File(outputDirectory, "debug_2_timescale.mp4");
            FileOutputStream muxedFileOutputStream = new FileOutputStream(muxedFile);
            muxed.getBox(muxedFileOutputStream.getChannel());
            muxedFileOutputStream.close();
        }

        IsoFile isoFile = ismvBuilder.build(qualities);
        if (writeSingleFile) {
            File allQualities = new File(outputDirectory, "debug_3_fragmented.mp4");
            //allQualities.createNewFile();
            FileOutputStream allQualis = new FileOutputStream(allQualities);
            isoFile.getBox(allQualis.getChannel());
            allQualis.close();
        }


        for (Track track : qualities.getTracks()) {
            String bitrate = Long.toString(manifestWriter.getBitrate(track));
            long trackId = track.getTrackMetaData().getTrackId();
            Iterator<Box> boxIt = isoFile.getBoxes().iterator();
            File mediaOutDir;
            if (track.getMediaHeaderBox() instanceof SoundMediaHeaderBox) {
                mediaOutDir = new File(outputDirectory, "audio");

            } else if (track.getMediaHeaderBox() instanceof VideoMediaHeaderBox) {
                mediaOutDir = new File(outputDirectory, "video");
            } else {
                System.err.println("Skipping Track with handler " + track.getHandler() + " and " + track.getMediaHeaderBox().getClass().getSimpleName());
                continue;
            }
            File bitrateOutputDir = new File(mediaOutDir, bitrate);
            bitrateOutputDir.mkdirs();
            LOG.finer("Created : " + bitrateOutputDir.getCanonicalPath());

            long[] fragmentTimes = manifestWriter.calculateFragmentDurations(track, qualities);
            long startTime = 0;
            int currentFragment = 0;
            while (boxIt.hasNext()) {
                Box b = boxIt.next();
                if (b instanceof MovieFragmentBox) {
                    assert ((MovieFragmentBox) b).getTrackCount() == 1;
                    if (((MovieFragmentBox) b).getTrackNumbers()[0] == trackId) {
                        FileOutputStream fos = new FileOutputStream(new File(bitrateOutputDir, Long.toString(startTime)));
                        startTime += fragmentTimes[currentFragment++];
                        FileChannel fc = fos.getChannel();
                        Box mdat = boxIt.next();
                        assert mdat.getType().equals("mdat");
                        b.getBox(fc); // moof
                        mdat.getBox(fc); // mdat
                        fc.truncate(fc.position());
                        fc.close();
                    }
                }

            }
        }
        FileWriter fw = new FileWriter(new File(outputDirectory, "Manifest"));
        fw.write(manifestWriter.getManifest(qualities));
        fw.close();

    }

    public static long pimpTimeScale(Movie movie, long targetTimeScale) {
        HashMap<String, Long> lcmPerType = new HashMap<String, Long>();
        for (Track t : movie.getTracks()) {
            // only adjust to tracks of the same type.
            long lcm;
            if (lcmPerType.containsKey(t.getHandler())) {
                lcm = lcmPerType.get(t.getHandler());
            } else {
                lcm = 1;
            }
            lcm = lcm(lcm, t.getTrackMetaData().getTimescale());
            lcmPerType.put(t.getHandler(), lcm);
        }

        long nuTimeScale = targetTimeScale;
        for (Long aLong : lcmPerType.values()) {
            long lcm = aLong;
            long nu = Math.abs(targetTimeScale - (targetTimeScale / lcm) * lcm);
            long old = Math.abs(targetTimeScale - nuTimeScale);
            if (nu > old) {
                nuTimeScale = (targetTimeScale / lcm) * lcm;
            }
          //  ((Math.round(((double) targetTimeScale / lcm)) * (lcm / trackTimeScale)) - ((double) targetTimeScale / lcm) * (lcm / trackTimeScale)))
        }
        return nuTimeScale;
    }

    /**
     * Gets a scale factor for a track so that all tracks are exactly stretched or
     * compressed by the same factor. This will ensure that frames that are shown
     * in the same instant are still shown at the same instant even after the change
     * of the timescale.
     * This is especially important if you are using two tracks with different FPS
     * and relying on I-frames being alligned - which is the case with Smooth Streaming.
     *
     * @param track
     * @param movie
     * @param targetTimeScale
     * @return
     */
    public static long getGoodScaleFactor(Track track, Movie movie, long targetTimeScale) {
        targetTimeScale = pimpTimeScale(movie, targetTimeScale);
        System.err.println("Nu TimeScale " + targetTimeScale) ;

        long lcm = 1;
        for (Track t : movie.getTracks()) {
            // only adjust to tracks of the same type.
            if (track.getHandler().equals(t.getHandler())) {
                lcm = lcm(lcm, t.getTrackMetaData().getTimescale());
            }
        }
        long trackTimeScale = track.getTrackMetaData().getTimescale();

        System.err.println("Scaling error: " + ((trackTimeScale * ((Math.round(((double) targetTimeScale / lcm)) * (lcm / trackTimeScale)) - ((double) targetTimeScale / lcm) * (lcm / trackTimeScale))) / targetTimeScale * 1000) + "ms pro s lag");
        //System.err.println("Scaling error: " + ((trackTimeScale *  (((double)(targetTimeScale / lcm) * (lcm / trackTimeScale)) - ((double)targetTimeScale / lcm) * (lcm / trackTimeScale))) / targetTimeScale * 1000) + "ms pro s lag"  );

        return (Math.round((double) targetTimeScale / lcm)) * (lcm / trackTimeScale);
    }


    /**
     * Modifies the <code>movie</code> param directly and return the modified <code>movie</code>.
     *
     * @param movie
     * @return the modified <code>movie</code> param
     */
    public Movie correctTimescale(Movie movie) {
        for (Track track : movie.getTracks()) {
            long tsScaler = track.getTrackMetaData().getTimescale();
            if (track.getCompositionTimeEntries() != null) {
                for (CompositionTimeToSample.Entry e : track.getCompositionTimeEntries()) {
                    tsScaler = gcd(tsScaler, e.getOffset());
                    if (tsScaler == 1) {
                        break;
                    }
                }
            }
            for (TimeToSampleBox.Entry e : track.getDecodingTimeEntries()) {
                tsScaler = gcd(tsScaler, e.getDelta());
                if (tsScaler == 1) {
                    break;
                }
            }
            if (track.getCompositionTimeEntries() != null) {
                for (CompositionTimeToSample.Entry e : track.getCompositionTimeEntries()) {
                    e.setOffset(l2i(e.getOffset() / tsScaler));
                }
            }

            for (TimeToSampleBox.Entry e : track.getDecodingTimeEntries()) {
                e.setDelta(l2i(e.getDelta() / tsScaler));
            }
            track.getTrackMetaData().setTimescale(track.getTrackMetaData().getTimescale() / tsScaler);
        }

        Movie nuMovie = new Movie();

        for (Track track : movie.getTracks()) {
            nuMovie.addTrack(new ChangeTimeScaleTrack(track, timeScale, getGoodScaleFactor(track, movie, timeScale)));
        }
        movie.setTracks(nuMovie.getTracks());
        return movie;
    }

}
