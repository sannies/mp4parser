package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

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
            return get((List<Box>)containerBox.getBoxes(), partList);
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
                    List<Box> children = (List<Box>) boxContainer.getBoxes();
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

