/*
 * Copyright 2011 Sebastian Annies, Hamburg
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
package com.googlecode.mp4parser.util;


import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Path {

    IsoFile isoFile;

    public Path(IsoFile isoFile) {
        this.isoFile = isoFile;
    }

    private static Pattern component = Pattern.compile("(....)(\\[(.*)\\])?");

    public String createPath(Box box) {
        return createPath(box, "");
    }

    private String createPath(Box box, String path) {
        if (box instanceof IsoFile) {
            assert box == isoFile;
            return path;
        } else {
            List<?> boxesOfBoxType = box.getParent().getBoxes(box.getClass());
            int index = boxesOfBoxType.indexOf(box);
            if (index != 0) {
                path = String.format("/%s[%d]", IsoFile.bytesToFourCC(box.getType()), index) + path;
            } else {
                path = String.format("/%s", IsoFile.bytesToFourCC(box.getType())) + path;
            }
            return createPath(box.getParent(), path);
        }
    }

    public Box getPath(String path) {
        return getPath(isoFile, path);
    }

    private Box getPath(Box box, String path) {
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.isEmpty()) {
            return box;
        } else {
            String later;
            String now;
            if (path.contains("/")) {
                later = path.substring(path.indexOf('/'));
                now = path.substring(0, path.indexOf('/'));
            } else {
                now = path;
                later = "";
            }

            Matcher m = component.matcher(now);
            if (m.matches()) {
                String type = m.group(1);
                int index = 0;
                if (m.group(2) != null) {
                    String indexString = m.group(3);
                    index = Integer.parseInt(indexString);
                }
                for (Box box1 : ((ContainerBox) box).getBoxes()) {
                    if (IsoFile.bytesToFourCC(box1.getType()).equals(type)) {
                        if (index == 0) {
                            return getPath(box1, later);
                        }
                        index--;
                    }
                }
                // could not find path
                return null;

            } else {
                throw new RuntimeException("invalid path.");
            }
        }

    }
}
