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
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.SoundMediaHeaderBox;
import com.coremedia.iso.boxes.VideoMediaHeaderBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.FragmentedMp4Builder;
import com.googlecode.mp4parser.authoring.builder.Mp4Builder;
import com.googlecode.mp4parser.authoring.builder.SyncSampleIntersectFinderImpl;
import com.googlecode.mp4parser.authoring.tracks.ChangeTimeScaleTrack;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.logging.Logger;

public class FlatPackageWriterImpl implements PackageWriter {
    private static Logger LOG  = Logger.getLogger(FlatPackageWriterImpl.class.getName());


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

        qualities = correctTimescale(qualities);

        IsoFile isoFile = ismvBuilder.build(qualities);
        if (writeSingleFile) {
            File allQualities = new File(outputDirectory, "all-qualities.mp4");
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


    public Movie correctTimescale(Movie movie) {
        Movie nuMovie = new Movie();
        for (Track track : movie.getTracks()) {
            nuMovie.addTrack(new ChangeTimeScaleTrack(track, timeScale, ChangeTimeScaleTrack.getGoodScaleFactor(track, movie, timeScale)));
        }
        return nuMovie;
    }

}
