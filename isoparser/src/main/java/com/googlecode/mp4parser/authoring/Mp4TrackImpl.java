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
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.GroupEntry;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.SampleGroupDescriptionBox;
import com.googlecode.mp4parser.boxes.mp4.samplegrouping.SampleToGroupBox;
import com.googlecode.mp4parser.util.Mp4Arrays;
import com.googlecode.mp4parser.util.Path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Represents a single track of an MP4 file.
 */
public class Mp4TrackImpl extends AbstractTrack {
    TrackBox trackBox;
    IsoFile[] fragments;
    private List<Sample> samples;
    private SampleDescriptionBox sampleDescriptionBox;
    private long[] decodingTimes;
    private List<CompositionTimeToSample.Entry> compositionTimeEntries;
    private long[] syncSamples = null;
    private List<SampleDependencyTypeBox.Entry> sampleDependencies;
    private TrackMetaData trackMetaData = new TrackMetaData();
    private String handler;
    private SubSampleInformationBox subSampleInformationBox = null;

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
        this.trackBox = trackBox;
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
        movieFragmentBoxes.addAll(((Box) trackBox.getParent()).getParent().getBoxes(MovieFragmentBox.class));
        for (IsoFile fragment : fragments) {
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

                        long sampleNumber = 1;
                        for (MovieFragmentBox movieFragmentBox : movieFragmentBoxes) {
                            List<TrackFragmentBox> trafs = movieFragmentBox.getBoxes(TrackFragmentBox.class);
                            for (TrackFragmentBox traf : trafs) {
                                if (traf.getTrackFragmentHeaderBox().getTrackId() == trackId) {
                                    sampleGroups = getSampleGroups(
                                            stbl.getBoxes(SampleGroupDescriptionBox.class),  // global descriptions
                                            Path.<SampleGroupDescriptionBox>getPaths((Container) traf, "sgpd"),  // local description
                                            Path.<SampleToGroupBox>getPaths((Container) traf, "sbgp"),
                                            sampleGroups, sampleNumber - 1);

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
                                                syncSamples = Mp4Arrays.copyOfAndAppend(syncSamples, sampleNumber);
                                            }
                                            sampleNumber++;
                                            first = false;
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }
        } else {
            sampleGroups = getSampleGroups(stbl.getBoxes(SampleGroupDescriptionBox.class), null, stbl.getBoxes(SampleToGroupBox.class), sampleGroups, 0);
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
        trackMetaData.setVolume(tkhd.getVolume());
        EditListBox elst = Path.getPath(trackBox, "edts/elst");
        MovieHeaderBox mvhd = Path.getPath(trackBox, "../mvhd");
        if (elst != null) {
            for (EditListBox.Entry e : elst.getEntries()) {
                edits.add(new Edit(e.getMediaTime(), mdhd.getTimescale(), e.getMediaRate(), (double) e.getSegmentDuration() / mvhd.getTimescale()));
            }
        }

    }

    private Map<GroupEntry, long[]> getSampleGroups(List<SampleGroupDescriptionBox> globalSgdbs, List<SampleGroupDescriptionBox> localSgdbs, List<SampleToGroupBox> sbgps,
                                                    Map<GroupEntry, long[]> sampleGroups, long startIndex) {

        for (SampleToGroupBox sbgp : sbgps) {
            int sampleNum = 0;
            for (SampleToGroupBox.Entry entry : sbgp.getEntries()) {
                if (entry.getGroupDescriptionIndex() > 0) {
                    GroupEntry groupEntry = null;
                    if (entry.getGroupDescriptionIndex() > 0xffff) {
                        for (SampleGroupDescriptionBox localSgdb : localSgdbs) {
                            if (localSgdb.getGroupingType().equals(sbgp.getGroupingType())) {
                                groupEntry = localSgdb.getGroupEntries().get((entry.getGroupDescriptionIndex() - 1) & 0xffff);
                            }
                        }
                    } else {
                        for (SampleGroupDescriptionBox globalSgdb : globalSgdbs) {
                            if (globalSgdb.getGroupingType().equals(sbgp.getGroupingType())) {
                                groupEntry = globalSgdb.getGroupEntries().get((entry.getGroupDescriptionIndex() - 1));
                            }
                        }
                    }
                    assert groupEntry != null;
                    long[] samples = sampleGroups.get(groupEntry);
                    if (samples == null) {
                        samples = new long[0];
                    }

                    long[] nuSamples = new long[l2i(entry.getSampleCount()) + samples.length];
                    System.arraycopy(samples, 0, nuSamples, 0, samples.length);
                    for (int i = 0; i < entry.getSampleCount(); i++) {
                        nuSamples[samples.length + i] = startIndex + sampleNum + i;
                    }
                    sampleGroups.put(groupEntry, nuSamples);

                }
                sampleNum += entry.getSampleCount();
            }
        }


        return sampleGroups;
    }

    public void close() throws IOException {
        Container c = trackBox.getParent();
        if (c instanceof BasicContainer) {
            ((BasicContainer) c).close();
        }
        if (fragments != null) {
            for (IsoFile fragment : fragments) {
                fragment.close();
            }
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
        if (syncSamples == null || syncSamples.length == samples.size()) {
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
