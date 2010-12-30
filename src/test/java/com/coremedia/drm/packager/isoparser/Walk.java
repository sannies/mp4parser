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

package com.coremedia.drm.packager.isoparser;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.BoxInterface;
import com.coremedia.iso.boxes.ContainerBox;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Walks through a ContainerBox and its children to see that no getter throws any exception.
 */
public final class Walk {
    private static final Collection<String> skipList = Arrays.asList("class",
            "boxes",
            "type",
            "userType",
            "size",
            "displayName",
            "contentSize",
            "offset",
            "header",
            "version",
            "flags",
            "isoFile",
            "parent",
            "data",
            "omaDrmData",
            "content",
            "tracks",
            "sampleSizeAtIndex",
            "offset",
            "sampleCount");

    private Walk() {
    }

    public static void through(ContainerBox container) throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        for (BoxInterface b : container.getBoxes()) {
            if (b instanceof ContainerBox) {
                Walk.through((ContainerBox) b);
            }
            if (b instanceof Box) {
                if (((Box) b).offset != b.calculateOffset()) {
                    throw new RuntimeException("Real offset " + ((Box) b).offset + " vs. calculated " + b.calculateOffset() + " Box: " + b);
                }
                b.toString(); // Just test if some execption is trown
                ((Box) b).getDisplayName(); // Just test if some execption is trown
                ((Box) b).getUserType();

                BeanInfo beanInfo = Introspector.getBeanInfo(b.getClass());
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

                for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                    String name = propertyDescriptor.getName();
                    if (!Walk.skipList.contains(name) &&
                            propertyDescriptor.getReadMethod() != null &&
                            !Box.class.isAssignableFrom(propertyDescriptor.getReadMethod().getReturnType())) {
                        propertyDescriptor.getReadMethod().invoke(b, (Object[]) null);
                    }
                }
            } else {
                throw new RuntimeException("dunno how top handle that");
            }
        }
    }
}
