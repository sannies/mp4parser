package org.mp4parser.muxer;

import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.boxes.iso14496.part12.*;
import org.mp4parser.boxes.iso23001.part7.AbstractSampleEncryptionBox;
import org.mp4parser.boxes.iso23001.part7.AbstractTrackEncryptionBox;
import org.mp4parser.boxes.iso23001.part7.CencSampleAuxiliaryDataFormat;
import org.mp4parser.muxer.tracks.CencEncryptedTrack;
import org.mp4parser.tools.Path;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * This track implementation is to be used when MP4 track is CENC encrypted.
 */
public class PiffMp4TrackImpl extends Mp4TrackImpl implements CencEncryptedTrack {

    private List<CencSampleAuxiliaryDataFormat> sampleEncryptionEntries;
    private UUID defaultKeyId;


    /**
     * Creates a track from a TrackBox and potentially fragments. Use <b>fragements parameter
     * only</b> to supply additional fragments that are not located in the main file.
     *
     * @param trackId      ID of the track to extract
     * @param isofile      the parsed MP4 file
     * @param randomAccess the RandomAccessSource to read the samples from
     * @param name         an arbitrary naem to identify track later - e.g. filename
     * @throws IOException if reading from underlying <code>DataSource</code> fails
     */
    public PiffMp4TrackImpl(final long trackId, Container isofile, RandomAccessSource randomAccess, String name) throws IOException {
        super(trackId, isofile, randomAccess, name);
        TrackBox trackBox = null;
        for (TrackBox box : Path.<TrackBox>getPaths(isofile, "moov/trak")) {
            if (box.getTrackHeaderBox().getTrackId() == trackId) {
                trackBox = box;
                break;
            }
        }
        SchemeTypeBox schm = Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schm[0]");
        assert schm != null && (schm.getSchemeType().equals("piff")) : "Track must be PIFF encrypted";

        sampleEncryptionEntries = new ArrayList<CencSampleAuxiliaryDataFormat>();

        final List<MovieExtendsBox> movieExtendsBoxes = Path.getPaths(isofile, "moov/mvex");
        if (!movieExtendsBoxes.isEmpty()) {


            for (MovieFragmentBox movieFragmentBox : isofile.getBoxes(MovieFragmentBox.class)) {
                List<TrackFragmentBox> trafs = movieFragmentBox.getBoxes(TrackFragmentBox.class);
                for (TrackFragmentBox traf : trafs) {
                    if (traf.getTrackFragmentHeaderBox().getTrackId() == trackId) {
                        AbstractTrackEncryptionBox tenc = Path.getPath(trackBox, "mdia[0]/minf[0]/stbl[0]/stsd[0]/enc.[0]/sinf[0]/schi[0]/uuid[0]");
                        assert tenc != null;
                        defaultKeyId = tenc.getDefault_KID();

                        long baseOffset;
                        if (traf.getTrackFragmentHeaderBox().hasBaseDataOffset()) {
                            baseOffset = traf.getTrackFragmentHeaderBox().getBaseDataOffset();
                        } else {
                            Iterator<Box> it = isofile.getBoxes().iterator();
                            baseOffset = 0;
                            for (Box b = it.next(); b != movieFragmentBox; b = it.next()) {
                                baseOffset += b.getSize();
                            }
                        }


                        List<TrackRunBox> truns = traf.getBoxes(TrackRunBox.class);
                        int sampleNo = 0;
                        AbstractSampleEncryptionBox senc = traf.getBoxes(AbstractSampleEncryptionBox.class).get(0);
                        for (CencSampleAuxiliaryDataFormat cencSampleAuxiliaryDataFormat : senc.getEntries()) {
                            sampleEncryptionEntries.add(cencSampleAuxiliaryDataFormat);
                        }

                    }
                }

            }

        }
    }


    public UUID getDefaultKeyId() {
        return defaultKeyId;
    }

    public boolean hasSubSampleEncryption() {
        return false;
    }

    public List<CencSampleAuxiliaryDataFormat> getSampleEncryptionEntries() {
        return sampleEncryptionEntries;
    }

    @Override
    public String toString() {
        return "PiffMp4TrackImpl{" +
                "handler='" + getHandler() + '\'' +
                '}';
    }

    @Override
    public String getName() {
        return "enc(" + super.getName() + ")";
    }

}
