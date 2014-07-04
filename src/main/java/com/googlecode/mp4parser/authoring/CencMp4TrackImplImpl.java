package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.googlecode.mp4parser.authoring.tracks.CencEncyprtedTrack;
import com.googlecode.mp4parser.boxes.cenc.CencSampleAuxiliaryDataFormat;
import com.googlecode.mp4parser.boxes.ultraviolet.SampleEncryptionBox;
import com.googlecode.mp4parser.util.Path;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static com.googlecode.mp4parser.util.CastUtils.l2i;

/**
 * Created by user on 04.07.2014.
 */
public class CencMp4TrackImplImpl extends Mp4TrackImpl implements CencEncyprtedTrack {

    private List<CencSampleAuxiliaryDataFormat> sampleEncryptionEntries;


    /**
     * Creates a track from a TrackBox and potentially fragments. Use <b>fragements parameter
     * only</b> to supply additional fragments that are not located in the main file.
     *
     * @param trackBox  the <code>TrackBox</code> describing the track.
     * @param fragments additional fragments if located in more than a single file
     */
    public CencMp4TrackImplImpl(TrackBox trackBox, IsoFile... fragments) {
        super(trackBox, fragments);
        sampleEncryptionEntries = new ArrayList<CencSampleAuxiliaryDataFormat>();
        SampleTableBox stbl = trackBox.getMediaBox().getMediaInformationBox().getSampleTableBox();
        SampleDescriptionBox sampleDescriptionBox = stbl.getSampleDescriptionBox();
        final List<MovieExtendsBox> movieExtendsBoxes = trackBox.getParent().getBoxes(MovieExtendsBox.class);
        long trackId = trackBox.getTrackHeaderBox().getTrackId();
        if (movieExtendsBoxes.size() > 0) {
            for (MovieExtendsBox mvex : movieExtendsBoxes) {
                final List<TrackExtendsBox> trackExtendsBoxes = mvex.getBoxes(TrackExtendsBox.class);
                for (TrackExtendsBox trex : trackExtendsBoxes) {
                    if (trex.getTrackId() == trackId) {
                        List<Long> syncSampleList = new LinkedList<Long>();

                        long sampleNumber = 1;
                        for (MovieFragmentBox movieFragmentBox : ((Box) trackBox.getParent()).getParent().getBoxes(MovieFragmentBox.class)) {
                            List<TrackFragmentBox> trafs = movieFragmentBox.getBoxes(TrackFragmentBox.class);
                            for (TrackFragmentBox traf : trafs) {
                                if (traf.getTrackFragmentHeaderBox().getTrackId() == trackId) {
                                    List<SampleEncryptionBox> sencs = traf.getBoxes(SampleEncryptionBox.class);
                                    for (SampleEncryptionBox senc : sencs) {

                                   // use saios!!!
                                        sampleEncryptionEntries.addAll(senc.getEntries());
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            SampleAuxiliaryInformationOffsetsBox saio = (SampleAuxiliaryInformationOffsetsBox) Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/saio[0]");
            SampleAuxiliaryInformationSizesBox saiz = (SampleAuxiliaryInformationSizesBox) Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/saiz[0]");
            SampleToChunkBox stco = (SampleToChunkBox) Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stco[0]");
            List<SampleToChunkBox.Entry> s2chunkEntries = trackBox.getSampleTableBox().getSampleToChunkBox().getEntries();
            SampleToChunkBox.Entry[] entries = s2chunkEntries.toArray(new SampleToChunkBox.Entry[s2chunkEntries.size()]);

            Container topLevel =  ((MovieBox)trackBox.getParent()).getParent();

            int s2cIndex = 0;
            SampleToChunkBox.Entry next = entries[s2cIndex++];
            int currentChunkNo = 0;
            int currentSamplePerChunk = 0;

            long nextFirstChunk = next.getFirstChunk();
            int nextSamplePerChunk = l2i(next.getSamplesPerChunk());

            int currentSampleNo = 1;
            int lastSampleNo = saiz.getSampleCount();



            do {
                for (int i = 0; i < currentSamplePerChunk; i++) {
                    CencSampleAuxiliaryDataFormat cadf = new CencSampleAuxiliaryDataFormat();


                    sampleEncryptionEntries.
                }

                currentChunkNo++;
                if (currentChunkNo == nextFirstChunk) {
                    currentSamplePerChunk = nextSamplePerChunk;
                    if (entries.length > s2cIndex) {
                        next = entries[s2cIndex++];
                        nextSamplePerChunk = l2i(next.getSamplesPerChunk());
                        nextFirstChunk = next.getFirstChunk();
                    } else {
                        nextSamplePerChunk = -1;
                        nextFirstChunk = Long.MAX_VALUE;
                    }
                }

            } while ((currentSampleNo += currentSamplePerChunk) <= lastSampleNo);

        }

    public UUID getKeyId() {
        return null;
    }

    public boolean hasSubSampleEncryption() {
        return false;
    }
}
