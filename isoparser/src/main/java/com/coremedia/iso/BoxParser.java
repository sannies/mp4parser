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
package com.coremedia.iso;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.Container;

import java.io.IOException;
import com.googlecode.mp4parser.DataSource;

/**
 * Basic interface to create boxes from a <code>IsoBufferWrapper</code> and its parent.
 */
public interface BoxParser {
    Box parseBox(DataSource in, Container parent) throws IOException;
}
