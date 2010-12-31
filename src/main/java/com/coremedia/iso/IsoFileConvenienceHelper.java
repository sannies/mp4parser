package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ChunkOffsetBox;
import com.coremedia.iso.boxes.DynamicChunkOffsetBox;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaInformationBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.TrackBox;

/**
 * A fine selection of useful methods.
 */
public class IsoFileConvenienceHelper {

    /**
     * The AutomaticChunkOffsetBox keeps track of the offset changes by recalculating
     * them on the fly if any change occurs to the file.
     *
     * @param isoFile will be changed!
     */
    public static void switchToAutomaticChunkOffsetBox(IsoFile isoFile) {
        MovieBox[] movieBoxes = isoFile.getBoxes(MovieBox.class);
        for (MovieBox movieBox : movieBoxes) {
            TrackBox[] trackBoxes = movieBox.getBoxes(TrackBox.class);
            for (TrackBox trackBox : trackBoxes) {
                SampleTableBox sampleTableBox = null;
                // Do not find the way to the sampleTableBox by many getBoxes(Class) calls since they need to much
                // object instantiation. Going this way here speeds up the process.
                for (Box mediaBoxe : trackBox.getBoxes()) {
                    if (mediaBoxe instanceof MediaBox) {
                        for (Box mediaInformationBoxe : ((MediaBox) mediaBoxe).getBoxes()) {
                            if (mediaInformationBoxe instanceof MediaInformationBox) {
                                for (Box sampleTableBoxe : ((MediaInformationBox) mediaInformationBoxe).getBoxes()) {
                                    if (sampleTableBoxe instanceof SampleTableBox) {
                                        sampleTableBox = (SampleTableBox) sampleTableBoxe;
                                    }
                                }
                            }
                        }
                    }
                }
                if (sampleTableBox != null) {
                    ChunkOffsetBox chunkOffsetBox = sampleTableBox.getChunkOffsetBox();
                    if (chunkOffsetBox == null) {
                        //todo fix this
                        System.err.println("Can't switch to AutomaticChunkOffsetBox. SampleTableBox " + sampleTableBox + " doesn't contain a ChunckOffsetBox!");
                    } else {
                        sampleTableBox.setChunkOffsetBox(new DynamicChunkOffsetBox(chunkOffsetBox));
                    }
                }
            }
        }
    }
}
