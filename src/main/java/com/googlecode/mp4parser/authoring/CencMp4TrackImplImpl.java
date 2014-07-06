package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.boxes.*;
import com.coremedia.iso.boxes.fragment.*;
import com.googlecode.mp4parser.authoring.tracks.CencEncyprtedTrack;
import com.googlecode.mp4parser.boxes.basemediaformat.TrackEncryptionBox;
import com.googlecode.mp4parser.boxes.cenc.CencSampleAuxiliaryDataFormat;
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
        assert schm != null && schm.getSchemeType().equals("cenc") : "Track must be CENC encrypted";

        sampleEncryptionEntries = new ArrayList<CencSampleAuxiliaryDataFormat>();
        long trackId = trackBox.getTrackHeaderBox().getTrackId();
        if (trackBox.getParent().getBoxes(MovieExtendsBox.class).size() > 0) {


            for (MovieFragmentBox movieFragmentBox : ((Box) trackBox.getParent()).getParent().getBoxes(MovieFragmentBox.class)) {
                List<TrackFragmentBox> trafs = movieFragmentBox.getBoxes(TrackFragmentBox.class);
                for (TrackFragmentBox traf : trafs) {
                    if (traf.getTrackFragmentHeaderBox().getTrackId() == trackId) {
                        TrackEncryptionBox tenc = (TrackEncryptionBox) Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schi[0]/tenc[0]");

                        Container base;
                        long baseOffset;
                        if (traf.getTrackFragmentHeaderBox().hasBaseDataOffset()) {
                            base = ((Box) trackBox.getParent()).getParent();
                            baseOffset = traf.getTrackFragmentHeaderBox().getBaseDataOffset();
                        } else {
                            base = movieFragmentBox;
                            baseOffset = 0;
                        }

                        FindSaioSaizPair saizSaioPair = new FindSaioSaizPair(traf).invoke();
                        SampleAuxiliaryInformationOffsetsBox saio = saizSaioPair.getSaio();
                        SampleAuxiliaryInformationSizesBox saiz = saizSaioPair.getSaiz();
                        // now we have the correct saio/saiz combo!
                        assert saio != null;
                        assert saio.getOffsets().size() == traf.getBoxes(TrackRunBox.class).size();
                        assert saiz != null;

                        List<TrackRunBox> truns = traf.getBoxes(TrackRunBox.class);
                        int sampleNo = 0;
                        for (int i = 0; i < saio.getOffsets().size(); i++) {
                            int numSamples = truns.get(i).getEntries().size();
                            long offset = saio.getOffsets().get(i);
                            long length = 0;

                            for (int j = sampleNo; j < sampleNo + numSamples; j++) {
                                length += saiz.getSize(j);
                            }
                            ByteBuffer trunsCencSampleAuxData = base.getByteBuffer(baseOffset + offset, length);
                            for (int j = sampleNo; j < sampleNo + numSamples; j++) {
                                int auxInfoSize = saiz.getSize(j);
                                sampleEncryptionEntries.add(
                                        parseCencAuxDataFormat(tenc.getDefaultIvSize(), trunsCencSampleAuxData, auxInfoSize)
                                );

                            }

                            sampleNo += numSamples;
                        }
                    }
                }

            }
        } else {
            TrackEncryptionBox tenc = (TrackEncryptionBox) Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schi[0]/tenc[0]");
            ChunkOffsetBox chunkOffsetBox = (ChunkOffsetBox) Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stco[0]");

            if (chunkOffsetBox == null) {
                chunkOffsetBox = (ChunkOffsetBox) Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/co64[0]");
            }
            long[] chunkSizes = trackBox.getSampleTableBox().getSampleToChunkBox().blowup(chunkOffsetBox.getChunkOffsets().length);


            FindSaioSaizPair saizSaioPair = new FindSaioSaizPair((Container) Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]")).invoke();
            SampleAuxiliaryInformationOffsetsBox saio = saizSaioPair.saio;
            SampleAuxiliaryInformationSizesBox saiz = saizSaioPair.saiz;

            Container topLevel = ((MovieBox) trackBox.getParent()).getParent();

            int currentSampleNo = 0;
            for (int i = 0; i < chunkSizes.length; i++) {
                long offset = saio.getOffsets().get(i);
                long size = 0;
                for (int j = currentSampleNo; j < currentSampleNo + chunkSizes[i]; j++) {
                    size += saiz.getSize(currentSampleNo + j);
                }
                ByteBuffer chunksCencSampleAuxData = topLevel.getByteBuffer(offset, size);
                for (int j = 0; j < chunkSizes[i]; j++) {
                    long auxInfoSize = saiz.getSize(currentSampleNo + i);
                    sampleEncryptionEntries.add(
                            parseCencAuxDataFormat(tenc.getDefaultIvSize(), chunksCencSampleAuxData, auxInfoSize)
                    );
                }
            }
        }
    }

    private CencSampleAuxiliaryDataFormat parseCencAuxDataFormat(int ivSize, ByteBuffer chunksCencSampleAuxData, long auxInfoSize) {
        CencSampleAuxiliaryDataFormat cadf = new CencSampleAuxiliaryDataFormat();
        cadf.iv = new byte[ivSize];
        chunksCencSampleAuxData.get(cadf.iv);
        if (auxInfoSize > ivSize) {
            int numOfPairs = IsoTypeReader.readUInt16(chunksCencSampleAuxData);
            cadf.pairs = new LinkedList<CencSampleAuxiliaryDataFormat.Pair>();
            while (numOfPairs-- > 0) {
                cadf.pairs.add(cadf.createPair(
                        IsoTypeReader.readUInt16(chunksCencSampleAuxData),
                        IsoTypeReader.readUInt32(chunksCencSampleAuxData)));
            }
        }
        return cadf;
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

    private class FindSaioSaizPair {
        private Container container;
        private SampleAuxiliaryInformationSizesBox saiz;
        private SampleAuxiliaryInformationOffsetsBox saio;

        public FindSaioSaizPair(Container container) {
            this.container = container;
        }

        public SampleAuxiliaryInformationSizesBox getSaiz() {
            return saiz;
        }

        public SampleAuxiliaryInformationOffsetsBox getSaio() {
            return saio;
        }

        public FindSaioSaizPair invoke() {
            List<SampleAuxiliaryInformationSizesBox> saizs = container.getBoxes(SampleAuxiliaryInformationSizesBox.class);
            List<SampleAuxiliaryInformationOffsetsBox> saios = container.getBoxes(SampleAuxiliaryInformationOffsetsBox.class);
            assert saizs.size() == saios.size();
            saiz = null;
            saio = null;

            for (int i = 0; i < saizs.size(); i++) {
                if (saiz == null && (saizs.get(i).getAuxInfoType() == null) || "cenc".equals(saizs.get(i).getAuxInfoType())) {
                    saiz = saizs.get(i);
                } else if (saiz != null && saiz.getAuxInfoType() == null && "cenc".equals(saizs.get(i).getAuxInfoType())) {
                    saiz = saizs.get(i);
                } else {
                    throw new RuntimeException("BULL.SHIT.");
                }
                if (saio == null && (saios.get(i).getAuxInfoType() == null) || "cenc".equals(saios.get(i).getAuxInfoType())) {
                    saio = saios.get(i);
                } else if (saio != null && saio.getAuxInfoType() == null && "cenc".equals(saios.get(i).getAuxInfoType())) {
                    saio = saios.get(i);
                } else {
                    throw new RuntimeException("BULL.SHIT.");
                }
            }
            return this;
        }
    }
}
