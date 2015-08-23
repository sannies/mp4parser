package com.mp4parser.rtp2dash;

import com.mp4parser.Box;
import com.mp4parser.Container;
import com.mp4parser.boxes.iso14496.part12.TrackFragmentBaseMediaDecodeTimeBox;
import com.mp4parser.streaming.MultiTrackFragmentedMp4Writer;
import com.mp4parser.streaming.StreamingTrack;
import com.mp4parser.tools.Path;
import mpeg.dash.schema.mpd._2011.RepresentationType;
import mpeg.dash.schema.mpd._2011.SegmentTemplateType;
import mpeg.dash.schema.mpd._2011.SegmentTimelineType;

import java.io.*;
import java.math.BigInteger;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;


public class DashFragmentedMp4Writer extends MultiTrackFragmentedMp4Writer {
    private static final Logger LOG = Logger.getLogger(DashFragmentedMp4Writer.class.getName());
    protected String mediaPattern = "$RepresentationID$/media-$Time$.mp4";
    protected String initPattern = "$RepresentationID$/init.mp4";

    File baseDir;
    File representationBaseDir;
    private final long adaptationSetId;
    private String representationId;

    public DashFragmentedMp4Writer(StreamingTrack source, File baseDir, long adaptationSetId, String representationId, OutputStream outputStream) {
        super(Collections.singletonList(source), outputStream);
        this.baseDir = baseDir;
        this.adaptationSetId = adaptationSetId;
        this.representationId = representationId;
        representationBaseDir = new File(baseDir, representationId);
        representationBaseDir.mkdir();
    }

    @Override
    protected Container createHeader() {
        Container initSegment = super.createHeader();
        try {
            WritableByteChannel wbc = new FileOutputStream(new File(representationBaseDir, "init.mp4")).getChannel();
            initSegment.writeContainer(wbc);
            wbc.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return initSegment;
    }


    @Override
    protected Container createFragment(StreamingTrack streamingTrack) {

        Container boxes = super.createFragment(streamingTrack);

        TrackFragmentBaseMediaDecodeTimeBox tfdt = Path.getPath(boxes, "moof[0]/traf[0]/tfdt[0]");
        try {

            assert tfdt != null;
            WritableByteChannel wbc = new FileOutputStream(new File(representationBaseDir, "media-" + tfdt.getBaseMediaDecodeTime() + ".mp4")).getChannel();
            LOG.info("created file for fragment of " + streamingTrack);

            boxes.writeContainer(wbc);
            wbc.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return boxes;
    }

    long getTime(File f) {
        String n = f.getName().substring(6);
        n = n.substring(0, n.indexOf("."));
        return Long.parseLong(n);
    }

    RepresentationType getRepresentation() {
        RepresentationType representationType = new RepresentationType();
        representationType.setId(representationId);
        SegmentTemplateType segmentTemplateType = new SegmentTemplateType();
        representationType.setSegmentTemplate(segmentTemplateType);
        SegmentTimelineType segmentTimelineType = new SegmentTimelineType();
        segmentTemplateType.setSegmentTimeline(segmentTimelineType);

        File[] files = representationBaseDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.contains("media-");
            }
        });
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File o1, File o2) {
                return (int) (getTime(o1) - getTime(o2));
            }
        });

        for (File file : files) {
            SegmentTimelineType.S s = new SegmentTimelineType.S();
            s.setT(BigInteger.valueOf(getTime(file)));
            segmentTimelineType.getS().add(s);
        }

        return representationType;
    }

    public long getAdaptationSetId() {
        return adaptationSetId;
    }
}
