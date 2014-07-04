package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.googlecode.mp4parser.authoring.tracks.CencEncyprtedTrack;
import com.googlecode.mp4parser.boxes.basemediaformat.TrackEncryptionBox;
import com.googlecode.mp4parser.boxes.cenc.CencSampleAuxiliaryDataFormat;
import com.googlecode.mp4parser.boxes.ultraviolet.SampleEncryptionBox;
import com.googlecode.mp4parser.util.Path;

import java.io.IOException;
import java.nio.ByteBuffer;
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
    public CencMp4TrackImplImpl(TrackBox trackBox, IsoFile... fragments) throws IOException {
        super(trackBox, fragments);

        SchemeTypeBox schm = (SchemeTypeBox) Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schm[0]");
        assert schm != null && schm.getSchemeType().equals("cenc"): "Track must be CENC encrypted";

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
            TrackEncryptionBox tenc = (TrackEncryptionBox) Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schi[0]/tenc[0]");
            List<SampleToChunkBox.Entry> s2chunkEntries = trackBox.getSampleTableBox().getSampleToChunkBox().getEntries();
            SampleToChunkBox.Entry[] entries = s2chunkEntries.toArray(new SampleToChunkBox.Entry[s2chunkEntries.size()]);

            Container topLevel = ((MovieBox) trackBox.getParent()).getParent();

            int s2cIndex = 0;
            SampleToChunkBox.Entry next = entries[s2cIndex++];
            int currentChunkNo = 0;
            int currentSamplePerChunk = 0;

            long nextFirstChunk = next.getFirstChunk();
            int nextSamplePerChunk = l2i(next.getSamplesPerChunk());

            int currentSampleNo = 0;
            int lastSampleNo = saiz.getSampleCount();


            do {
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
                long offset = saio.getOffsets().get(currentChunkNo -1);
                long size = 0;
                if (saiz.getDefaultSampleInfoSize() == 0) {
                    for (int i = currentSampleNo; i < currentSampleNo + currentSamplePerChunk; i++) {
                        size += saiz.getSampleInfoSizes().get(currentSampleNo + i);
                    }
                } else {
                    size += currentSamplePerChunk * saiz.getDefaultSampleInfoSize();
                }
                ByteBuffer chunksCencSampleAuxData = topLevel.getByteBuffer(
                        offset, size);
                for (int i = 0; i < currentSamplePerChunk; i++) {
                    CencSampleAuxiliaryDataFormat cadf = new CencSampleAuxiliaryDataFormat();
                    sampleEncryptionEntries.add(cadf);
                    long auxInfoSize;
                    if (saiz.getDefaultSampleInfoSize() == 0) {
                        auxInfoSize = saiz.getSampleInfoSizes().get(currentSampleNo + i );
                    } else {
                        auxInfoSize = saiz.getDefaultSampleInfoSize();
                    }


                    cadf.iv = new byte[tenc.getDefaultIvSize()];
                    chunksCencSampleAuxData.get(cadf.iv);
                    if (auxInfoSize > tenc.getDefaultIvSize()) {
                        int numOfPairs = IsoTypeReader.readUInt16(chunksCencSampleAuxData);
                        cadf.pairs = new LinkedList<CencSampleAuxiliaryDataFormat.Pair>();
                        while (numOfPairs-- > 0) {
                            cadf.pairs.add(cadf.createPair(
                                    IsoTypeReader.readUInt16(chunksCencSampleAuxData),
                                    IsoTypeReader.readUInt32(chunksCencSampleAuxData)));
                        }
                    }

                }

            } while ((currentSampleNo += currentSamplePerChunk) < lastSampleNo);

        }
    }

    public UUID getKeyId() {
        return null;
    }

    public boolean hasSubSampleEncryption() {
        return false;
    }

    public List<CencSampleAuxiliaryDataFormat> getSampleEncryptionEntries() {
        return sampleEncryptionEntries;
    }

    @Override
    public String toString() {
        return "CencMp4TrackImpl{" +
                "handler='" + getHandler() + '\'' +
                '}';
    }
}
