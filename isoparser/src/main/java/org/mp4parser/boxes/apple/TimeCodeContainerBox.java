/*  
 * Copyright 2008 CoreMedia AG, Hamburg
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

package org.mp4parser.boxes.apple;

import org.mp4parser.boxes.iso14496.part12.TrackBox;
import org.mp4parser.support.AbstractContainerBox;

/**
 * <h1>4cc = "{@value #TYPE}"</h1>
 * <code>
 * Box Type: 'tmcd'<br>
 * Container: {@link TrackBox} ('gmhd')<br>
 * Mandatory: No<br>
 * Quantity: Zero or one<br><br>
 * </code>
 * This box is a container for the 'tcmi' box.
 */
public class TimeCodeContainerBox extends AbstractContainerBox {
    public static final String TYPE = "tmcd";

    public TimeCodeContainerBox() {
        super(TYPE);
    }
}
