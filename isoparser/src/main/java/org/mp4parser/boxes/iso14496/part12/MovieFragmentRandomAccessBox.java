/*
 * Copyright 2009 castLabs GmbH, Berlin
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

package org.mp4parser.boxes.iso14496.part12;

import org.mp4parser.support.AbstractContainerBox;

/**
 * The Movie Fragment Random Access Box ('mfra') provides a table which may assist
 * readers in finding random access points in a file using movie fragments.It contains
 * a track fragment random access box for each track for which information is provided
 * (which may notbe all tracks).
 */
public class MovieFragmentRandomAccessBox extends AbstractContainerBox {
    public static final String TYPE = "mfra";

    public MovieFragmentRandomAccessBox() {
        super(TYPE);
    }

}
