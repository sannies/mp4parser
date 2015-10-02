/*
 * Copyright 2012 Sebastian Annies, Hamburg
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
package org.mp4parser.tools;


import org.mp4parser.Box;
import org.mp4parser.Container;
import org.mp4parser.ParsableBox;
import org.mp4parser.support.AbstractContainerBox;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Path {

    static Pattern component = Pattern.compile("(....|\\.\\.)(\\[(.*)\\])?");

    private Path() {
    }

    public static <T extends Box> T getPath(Box parsableBox, String path) {
        List<T> all = getPaths(parsableBox, path, true);
        return all.isEmpty() ? null : all.get(0);
    }

    public static <T extends Box> T getPath(Container container, String path) {
        List<T> all = getPaths(container, path, true);
        return all.isEmpty() ? null : all.get(0);
    }

    public static <T extends Box> T getPath(AbstractContainerBox containerBox, String path) {
        List<T> all = getPaths(containerBox, path, true);
        return all.isEmpty() ? null : all.get(0);
    }


    public static <T extends Box> List<T> getPaths(Box box, String path) {
        return getPaths(box, path, false);
    }

    public static <T extends Box> List<T> getPaths(Container container, String path) {
        return getPaths(container, path, false);
    }

    private static <T extends Box> List<T> getPaths(AbstractContainerBox container, String path, boolean singleResult) {
        return getPaths((Object) container, path, singleResult);
    }

    private static <T extends Box> List<T> getPaths(Container container, String path, boolean singleResult) {
        return getPaths((Object) container, path, singleResult);
    }

    private static <T extends Box> List<T> getPaths(ParsableBox parsableBox, String path, boolean singleResult) {
        return getPaths((Object) parsableBox, path, singleResult);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Box> List<T> getPaths(Object thing, String path, boolean singleResult) {
        if (path.startsWith("/")) {
            throw new RuntimeException("Cannot start at / - only relative path expression into the structure are allowed");
        }

        if (path.length() == 0) {
            if (thing instanceof ParsableBox) {
                return Collections.singletonList((T) thing);
            } else {
                throw new RuntimeException("Result of path expression seems to be the root container. This is not allowed!");
            }
        } else {
            String later;
            String now;
            if (path.contains("/")) {
                later = path.substring(path.indexOf('/') + 1);
                now = path.substring(0, path.indexOf('/'));
            } else {
                now = path;
                later = "";
            }

            Matcher m = component.matcher(now);
            if (m.matches()) {
                String type = m.group(1);
                if ("..".equals(type)) {
                    throw new RuntimeException(".. notation no longer allowed");
                } else {
                    if (thing instanceof Container) {
                        int index = -1;
                        if (m.group(2) != null) {
                            // we have a specific index
                            String indexString = m.group(3);
                            index = Integer.parseInt(indexString);
                        }
                        List<T> children = new LinkedList<T>();
                        int currentIndex = 0;
                        // I'm suspecting some Dalvik VM to create indexed loops from for-each loops
                        // using the iterator instead makes sure that this doesn't happen
                        // (and yes - it could be completely useless)
                        Iterator<Box> iterator = ((Container) thing).getBoxes().iterator();
                        while (iterator.hasNext()) {
                            Box box1 = iterator.next();
                            if (box1.getType().matches(type)) {
                                if (index == -1 || index == currentIndex) {
                                    children.addAll(Path.<T>getPaths(box1, later, singleResult));
                                }
                                currentIndex++;
                            }
                            if ((singleResult || index >= 0) && !children.isEmpty()) {
                                return children;
                            }
                        }
                        return children;
                    } else {
                        return Collections.emptyList();
                    }
                }
            } else {
                throw new RuntimeException(now + " is invalid path.");
            }

        }

    }


    public static boolean isContained(Container ref, Box box, String path) {
        return getPaths(ref, path).contains(box);
    }
}
