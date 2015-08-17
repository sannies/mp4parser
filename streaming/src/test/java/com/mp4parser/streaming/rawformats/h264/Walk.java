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

package com.mp4parser.streaming.rawformats.h264;

import com.mp4parser.Box;
import com.mp4parser.Container;
import com.mp4parser.ParsableBox;
import com.mp4parser.support.AbstractBox;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Walks through a Container and its children to see that no getter throws any exception.
 */
public final class Walk {
    private static final Collection<String> skipList = Arrays.asList("class",
            "boxes",
            "type",
            "size",
            "displayName",
            "contentSize",
            "offset",
            "header",
            "version",
            "flags",
            "isoFile",
            "parent",
            "omaDrmData",
            "content",
            "tracks",
            "sampleSizeAtIndex",
            "offset",
            "sampleCount");

    private Walk() {
    }

    public static void through(Container container) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        for (Box b : container.getBoxes()) {
            List<Box> myBoxes = (List<Box>) container.getBoxes(b.getClass());
            boolean found = false;
            for (Box myBox : myBoxes) {
                if (myBox == b) {
                    found = true;
                }
            }
            if (!found) {
                throw new RuntimeException("Didn't find the box");
            }

            if (b instanceof Container) {
                Walk.through((Container) b);
            }


            b.toString(); // Just test if some execption is trown

            BeanInfo beanInfo = Introspector.getBeanInfo(b.getClass());
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String name = propertyDescriptor.getName();
                if (!Walk.skipList.contains(name) &&
                        propertyDescriptor.getReadMethod() != null &&
                        !ParsableBox.class.isAssignableFrom(propertyDescriptor.getReadMethod().getReturnType())) {
                    propertyDescriptor.getReadMethod().invoke(b, (Object[]) null);
                }

            }
            if (b instanceof AbstractBox) {
                assert ((AbstractBox) b).isParsed() : "Box (" + b.getClass().getSimpleName() + ") is not parsed.";
            }

        }
    }
}
