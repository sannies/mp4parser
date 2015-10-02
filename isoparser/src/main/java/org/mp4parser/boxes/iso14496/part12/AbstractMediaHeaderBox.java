/*
 * Copyright 2011 Sebastian Annies, Hamburg, Germany
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

import org.mp4parser.support.AbstractFullBox;

/**
 * A common superclass for all MediaInformationHeaderBoxes. E.g.
 * VideoMediaHeaderBox, SoundMediaHeaderBox &amp; HintMediaHeaderBox
 */
public abstract class AbstractMediaHeaderBox extends AbstractFullBox {
    protected AbstractMediaHeaderBox(String type) {
        super(type);
    }
}
