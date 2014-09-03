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
package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.fragment.*;
import com.coremedia.iso.boxes.mdat.SampleList;
import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.util.Path;

import java.io.IOException;
import java.util.*;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Represents a single track of an MP4 file.
 */
public class Mp4TrackImpl extends AbstractTrack {
    private List<Sample> samples;
    private SampleDescriptionBox sampleDescriptionBox;
    private long[] decodingTimes;
    private List<CompositionTimeToSample.Entry> compositionTimeEntries;
    private long[] syncSamples = new long[0];
    private List<SampleDependencyTypeBox.Entry> sampleDependencies;
    private TrackMetaData trackMetaData = new TrackMetaData();
    private String handler;
    private SubSampleInformationBox subSampleInformationBox = null;

    TrackBox trackBox;
    IsoFile[] fragments;

    /**
     * Creates a track from a TrackBox and potentially fragments. Use <b>fragements parameter
     * only</b> to supply additional fragments that are not located in the main file.
     *
     * @param name      a name for the track for better identification
     * @param trackBox  the <code>TrackBox</code> describing the track.
     * @param fragments additional fragments if located in more than a single file
     */
    public Mp4TrackImpl(String name, TrackBox trackBox, IsoFile... fragments) {
        super(name);
        final long trackId = trackBox.getTrackHeaderBox().getTrackId();
        samples = new SampleList(trackBox, fragments);
        SampleTableBox stbl = trackBox.getMediaBox().getMediaInformationBox().getSampleTableBox();
        handler = trackBox.getMediaBox().getHandlerBox().getHandlerType();

        List<TimeToSampleBox.Entry> decodingTimeEntries = new ArrayList<TimeToSampleBox.Entry>();
        compositionTimeEntries = new ArrayList<CompositionTimeToSample.Entry>();
        sampleDependencies = new ArrayList<SampleDependencyTypeBox.Entry>();

        decodingTimeEntries.addAll(stbl.getTimeToSampleBox().getEntries());
        if (stbl.getCompositionTimeToSample() != null) {
            compositionTimeEntries.addAll(stbl.getCompositionTimeToSample().getEntries());
        }
        if (stbl.getSampleDependencyTypeBox() != null) {
            sampleDependencies.addAll(stbl.getSampleDependencyTypeBox().getEntries());
        }
        if (stbl.getSyncSampleBox() != null) {
            syncSamples = stbl.getSyncSampleBox().getSampleNumber();
        }
        subSampleInformationBox = Path.getPath(stbl, "subs");

        // gather all movie fragment boxes from the fragments
        List<MovieFragmentBox> movieFragmentBoxes = new ArrayList<MovieFragmentBox>();
        movieFragmentBoxes.addAll(((Box)trackBox.getParent()).getParent().getBoxes(MovieFragmentBox.class));
        for(IsoFile fragment : fragments) {
            movieFragmentBoxes.addAll(fragment.getBoxes(MovieFragmentBox.class));
        }

        sampleDescriptionBox = stbl.getSampleDescriptionBox();
        int lastSubsSample = 0;
        final List<MovieExtendsBox> movieExtendsBoxes = trackBox.getParent().getBoxes(MovieExtendsBox.class);
        if (movieExtendsBoxes.size() > 0) {
            for (MovieExtendsBox mvex : movieExtendsBoxes) {
                final List<TrackExtendsBox> trackExtendsBoxes = mvex.getBoxes(TrackExtendsBox.class);
                for (TrackExtendsBox trex : trackExtendsBoxes) {
                    if (trex.getTrackId() == trackId) {
                        List<SubSampleInformationBox> subss = Path.getPaths(((Box) trackBox.getParent()).getParent(), "/moof/traf/subs");
                        if (subss.size() > 0) {
                            subSampleInformationBox = new SubSampleInformationBox();
                        }
                        List<Long> syncSampleList = new LinkedList<Long>();

                        long sampleNumber = 1;
                        for (MovieFragmentBox movieFragmentBox : movieFragmentBoxes) {
                            List<TrackFragmentBox> trafs = movieFragmentBox.getBoxes(TrackFragmentBox.class);
                            for (TrackFragmentBox traf : trafs) {
                                if (traf.getTrackFragmentHeaderBox().getTrackId() == trackId) {


                                    SubSampleInformationBox subs = Path.getPath(traf, "subs");
                                    if (subs != null) {
                                        long difFromLastFragment = sampleNumber - lastSubsSample - 1;
                                        for (SubSampleInformationBox.SubSampleEntry subSampleEntry : subs.getEntries()) {
                                            SubSampleInformationBox.SubSampleEntry se = new SubSampleInformationBox.SubSampleEntry();
                                            se.getSubsampleEntries().addAll(subSampleEntry.getSubsampleEntries());
                                            if (difFromLastFragment != 0) {
                                                se.setSampleDelta(difFromLastFragment + subSampleEntry.getSampleDelta());
                                                difFromLastFragment = 0;
                                            } else {
                                                se.setSampleDelta(subSampleEntry.getSampleDelta());
                                            }
                                            subSampleInformationBox.getEntries().add(se);
                                        }
                                    }

                                    List<TrackRunBox> truns = traf.getBoxes(TrackRunBox.class);
                                    for (TrackRunBox trun : truns) {
                                        final TrackFragmentHeaderBox tfhd = ((TrackFragmentBox) trun.getParent()).getTrackFragmentHeaderBox();
                                        boolean first = true;
                                        for (TrackRunBox.Entry entry : trun.getEntries()) {
                                            if (trun.isSampleDurationPresent()) {
                                                if (decodingTimeEntries.size() == 0 ||
                                                        decodingTimeEntries.get(decodingTimeEntries.size() - 1).getDelta() != entry.getSampleDuration()) {
                                                    decodingTimeEntries.add(new TimeToSampleBox.Entry(1, entry.getSampleDuration()));
                                                } else {
                                                    TimeToSampleBox.Entry e = decodingTimeEntries.get(decodingTimeEntries.size() - 1);
                                                    e.setCount(e.getCount() + 1);
                                                }
                                            } else {
                                                if (tfhd.hasDefaultSampleDuration()) {
                                                    decodingTimeEntries.add(new TimeToSampleBox.Entry(1, tfhd.getDefaultSampleDuration()));
                                                } else {
                                                    decodingTimeEntries.add(new TimeToSampleBox.Entry(1, trex.getDefaultSampleDuration()));
                                                }
                                            }

                                            if (trun.isSampleCompositionTimeOffsetPresent()) {
                                                if (compositionTimeEntries.size() == 0 ||
                                                        compositionTimeEntries.get(compositionTimeEntries.size() - 1).getOffset() != entry.getSampleCompositionTimeOffset()) {
                                                    compositionTimeEntries.add(new CompositionTimeToSample.Entry(1, l2i(entry.getSampleCompositionTimeOffset())));
                                                } else {
                                                    CompositionTimeToSample.Entry e = compositionTimeEntries.get(compositionTimeEntries.size() - 1);
                                                    e.setCount(e.getCount() + 1);
                                                }
                                            }
                                            final SampleFlags sampleFlags;
                                            if (trun.isSampleFlagsPresent()) {
                                                sampleFlags = entry.getSampleFlags();
                                            } else {
                                                if (first && trun.isFirstSampleFlagsPresent()) {
                                                    sampleFlags = trun.getFirstSampleFlags();
                                                } else {
                                                    if (tfhd.hasDefaultSampleFlags()) {
                                                        sampleFlags = tfhd.getDefaultSampleFlags();
                                                    } else {
                                                        sampleFlags = trex.getDefaultSampleFlags();
                                                    }
                                                }
                                            }
                                            if (sampleFlags != null && !sampleFlags.isSampleIsDifferenceSample()) {
                                                //iframe
                                                syncSampleList.add(sampleNumber);
                                            }
                                            sampleNumber++;
                                            first = false;
                                        }
                                    }
                                }
                            }
                        }
                        // Warning: Crappy code
                        long[] oldSS = syncSamples;
                        syncSamples = new long[syncSamples.length + syncSampleList.size()];
                        System.arraycopy(oldSS, 0, syncSamples, 0, oldSS.length);
                        final Iterator<Long> iterator = syncSampleList.iterator();
                        int i = oldSS.length;
                        while (iterator.hasNext()) {
                            Long syncSampleNumber = iterator.next();
                            syncSamples[i++] = syncSampleNumber;
                        }
                    }
                }
            }
        }
        decodingTimes = TimeToSampleBox.blowupTimeToSamples(decodingTimeEntries);

        MediaHeaderBox mdhd = trackBox.getMediaBox().getMediaHeaderBox();
        TrackHeaderBox tkhd = trackBox.getTrackHeaderBox();

        trackMetaData.setTrackId(tkhd.getTrackId());
        trackMetaData.setCreationTime(mdhd.getCreationTime());
        trackMetaData.setLanguage(mdhd.getLanguage());

        trackMetaData.setModificationTime(mdhd.getModificationTime());
        trackMetaData.setTimescale(mdhd.getTimescale());
        trackMetaData.setHeight(tkhd.getHeight());
        trackMetaData.setWidth(tkhd.getWidth());
        trackMetaData.setLayer(tkhd.getLayer());
        trackMetaData.setMatrix(tkhd.getMatrix());
        EditListBox elst = Path.getPath(trackBox, "edts/elst");
        MovieHeaderBox mvhd = Path.getPath(trackBox, "../mvhd");
        if (elst != null) {
            for (EditListBox.Entry e : elst.getEntries()) {
                edits.add(new Edit(mvhd.getTimescale(), e.getSegmentDuration(), e.getMediaTime(), e.getMediaRate()));
            }
        }

    }

    public void close() throws IOException {
        Container c = trackBox.getParent();
        if (c instanceof BasicContainer) {
            ((BasicContainer) c).close();
        }
        for (IsoFile fragment : fragments) {
            fragment.close();
        }


    }

    public List<Sample> getSamples() {
        return samples;
    }

    public synchronized long[] getSampleDurations() {
        return decodingTimes;
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return sampleDescriptionBox;
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        return compositionTimeEntries;
    }

    public long[] getSyncSamples() {
        if (syncSamples.length == samples.size()) {
            return null;
        } else {
            return syncSamples;
        }
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        return sampleDependencies;
    }

    public TrackMetaData getTrackMetaData() {
        return trackMetaData;
    }

    public String getHandler() {
        return handler;
    }

    public SubSampleInformationBox getSubsampleInformationBox() {
        return subSampleInformationBox;
    }


}
