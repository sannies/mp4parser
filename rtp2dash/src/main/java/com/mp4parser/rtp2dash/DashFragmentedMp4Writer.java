package com.mp4parser.rtp2dash;

import com.mp4parser.Box;
import com.mp4parser.Container;
import com.mp4parser.IsoFile;
import com.mp4parser.boxes.iso14496.part12.TrackFragmentBaseMediaDecodeTimeBox;
import com.mp4parser.boxes.iso14496.part12.TrackFragmentBox;
import com.mp4parser.boxes.iso14496.part12.TrackRunBox;
import com.mp4parser.boxes.sampleentry.VisualSampleEntry;
import com.mp4parser.streaming.MultiTrackFragmentedMp4Writer;
import com.mp4parser.streaming.StreamingSample;
import com.mp4parser.streaming.StreamingTrack;
import com.mp4parser.tools.Path;
import mpeg.dash.schema.mpd._2011.RepresentationType;
import mpeg.dash.schema.mpd._2011.SegmentTemplateType;
import mpeg.dash.schema.mpd._2011.SegmentTimelineType;

import java.io.*;
import java.math.BigInteger;
import java.nio.channels.WritableByteChannel;
import java.util.*;
import java.util.logging.Logger;


public class DashFragmentedMp4Writer extends MultiTrackFragmentedMp4Writer {
    private static final Logger LOG = Logger.getLogger(DashFragmentedMp4Writer.class.getName());
    private File baseDir;
    private File representationBaseDir;
    private final long adaptationSetId;
    private String representationId;
    private StreamingTrack source;



    public DashFragmentedMp4Writer(StreamingTrack source, File baseDir, long adaptationSetId, String representationId, OutputStream outputStream) throws IOException {
        super(Collections.singletonList(source), outputStream);
        this.baseDir = baseDir;
        this.source = source;
        this.adaptationSetId = adaptationSetId;
        this.representationId = representationId;
        representationBaseDir = new File(baseDir, representationId);
        representationBaseDir.mkdir();
        this.timeOut = 10000;
    }

    public StreamingTrack getSource() {
        return source;
    }

    public boolean isClosed() {
        return !source.hasMoreSamples();
    }

    @Override
    protected Container createHeader() {
        Container initSegment = super.createHeader();
        try {
            WritableByteChannel wbc = new FileOutputStream(new File(representationBaseDir, "init.mp4")).getChannel();
            initSegment.writeContainer(wbc);
            wbc.close();
        } catch (IOException e) {
            LOG.severe(e.getLocalizedMessage());
            throw new RuntimeException(e);
        }
        return initSegment;
    }

    @Override
    protected Container createFragment(StreamingTrack streamingTrack) throws IOException {

        Container boxes = super.createFragment(streamingTrack);

        TrackFragmentBaseMediaDecodeTimeBox tfdt = Path.getPath(boxes, "moof[0]/traf[0]/tfdt[0]");

        assert tfdt != null;
        File f = new File(representationBaseDir, "media-" + tfdt.getBaseMediaDecodeTime() + ".mp4");
        WritableByteChannel wbc = new FileOutputStream(f).getChannel();
        //LOG.info("created file for fragment of " + streamingTrack);

        boxes.writeContainer(wbc);
        wbc.close();
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
        representationType.setCodecs(DashHelper.getRfc6381Codec(source.getSampleDescriptionBox().getSampleEntry()));
        representationType.setStartWithSAP(1L);
        if ("vide".equals(this.source.getHandler())) {
            representationType.setMimeType("video/mp4");
            representationType.setWidth((long) ((VisualSampleEntry)source.getSampleDescriptionBox().getSampleEntry()).getWidth());
            representationType.setHeight((long) ((VisualSampleEntry)source.getSampleDescriptionBox().getSampleEntry()).getHeight());
        } else if ("soun".equals(this.source.getHandler())) {
            representationType.setMimeType("audio/mp4");
        } else {
            LOG.severe("I can't build Representation for track with handler " + this.source.getHandler());
            return null;
        }
        SegmentTemplateType segmentTemplateType = new SegmentTemplateType();
        segmentTemplateType.setTimescale(source.getTimescale());
        representationType.setSegmentTemplate(segmentTemplateType);
        SegmentTimelineType segmentTimelineType = new SegmentTimelineType();
        segmentTemplateType.setSegmentTimeline(segmentTimelineType);
        segmentTemplateType.setInitializationAttribute(representationId + "/init.mp4");
        segmentTemplateType.setMedia(representationId + "/media-$Time$.mp4");

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
        long fileSize = 0;
        long representationDuration = 0;
        for (File file : files) {
            fileSize += file.length();
            long d = 0;
            try {
                FileInputStream fis = new FileInputStream(file);
                IsoFile isoFile = new IsoFile(fis.getChannel());
                TrackRunBox trun = Path.getPath(isoFile, "moof[0]/traf[0]/trun[0]");
                for (TrackRunBox.Entry entry : trun.getEntries()) {
                    d += entry.getSampleDuration();
                }
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            SegmentTimelineType.S sOld = segmentTimelineType.getS().size() > 0 ? segmentTimelineType.getS().get(segmentTimelineType.getS().size() - 1) : null;
            if (sOld != null && sOld.getD().equals(BigInteger.valueOf(d))) {
                BigInteger r = sOld.getR();
                if (r == null || r.equals(BigInteger.ZERO)) {
                    sOld.setR(BigInteger.ONE);
                } else {
                    sOld.setR(r.add(BigInteger.ONE));
                }
            } else {
                SegmentTimelineType.S s = new SegmentTimelineType.S();
                s.setD(BigInteger.valueOf(d));
                s.setT(BigInteger.valueOf(getTime(file)));
                segmentTimelineType.getS().add(s);
            }
            representationDuration += d;
        }
        representationType.setBandwidth((fileSize * 8 * this.source.getTimescale()) / representationDuration);


        return representationType;
    }

    public long getAdaptationSetId() {
        return adaptationSetId;
    }
}
