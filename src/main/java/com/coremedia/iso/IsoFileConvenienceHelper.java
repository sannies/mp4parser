package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ChunkOffsetBox;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.DynamicChunkOffsetBox;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaInformationBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.TrackBox;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A fine selection of useful methods.
 *
 * @author Andre John Mas
 * @author Sebastian Annies
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

    public static Box get(ContainerBox containerBox, String path) {

        String[] parts = path.split("/");
        if (parts.length == 0) {
            return null;
        }

        List<String> partList = new ArrayList<String>(Arrays.asList(parts));

        if ("".equals(partList.get(0))) {
            partList.remove(0);
        }

        if (partList.size() > 0) {
            List<Box> boxes = Arrays.asList(containerBox.getBoxes());

            return get(boxes, partList);
        }
        return null;
    }

    private static Box get(List<Box> boxes, List<String> path) {


        String typeInPath = path.remove(0);

        for (Box box : boxes) {
            if (box instanceof ContainerBox) {
                ContainerBox boxContainer = (ContainerBox) box;
                String type = IsoFile.bytesToFourCC(boxContainer.getType());

                if (typeInPath.equals(type)) {
                    List<Box> children = Arrays.asList(boxContainer.getBoxes());
                    if (path.size() > 0) {
                        if (children.size() > 0) {
                            return get(children, path);
                        }
                    } else {
                        return box;
                    }
                }

            } else {
                String type = IsoFile.bytesToFourCC(box.getType());

                if (path.size() == 0 && typeInPath.equals(type)) {
                    return box;
                }

            }

        }

        return null;
    }
}

