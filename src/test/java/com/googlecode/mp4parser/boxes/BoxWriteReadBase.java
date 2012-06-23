package com.googlecode.mp4parser.boxes;


import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import junit.framework.Assert;
import org.junit.Assume;
import org.junit.Test;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.nio.channels.Channels;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class BoxWriteReadBase<T extends Box> {

    private static final Collection<String> skipList = Arrays.asList(
            "class",
            "flags",
            "isoFile",
            "parent",
            "parsed",
            "size",
            "type",
            "userType",
            "version");


    public abstract Class<T> getBoxUnderTest();

    public abstract void setupProperties(Map<String, Object> addPropsHere);


    @Test
    public void roundtrip() throws Exception {
        Class<T> clazz = getBoxUnderTest();
        Constructor<T> constructor = clazz.getConstructor();
        T box = constructor.newInstance();
        BeanInfo beanInfo = Introspector.getBeanInfo(box.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Map<String, Object> props =  new HashMap<String, Object>();
        setupProperties(props);
        for (String property : props.keySet()) {
            boolean found = false;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (property.equals(propertyDescriptor.getName())) {
                    found = true;
                    propertyDescriptor.getWriteMethod().invoke(box, props.get(property));
                    // do the next assertion on string level to not trap into the long vs java.lang.Long pitfall
                    Assert.assertEquals("The symmetry between getter/setter is not given.", props.get(property).toString(), (Object) propertyDescriptor.getReadMethod().invoke(box).toString());
                }
            }
            if (!found) {
                Assert.fail("Could not find any property descriptor for " + property);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        box.getBox(Channels.newChannel(baos));
        Assert.assertEquals(box.getSize(), baos.size());
        IsoFile singleBoxIsoFile = new IsoFile(Channels.newChannel(new ByteArrayInputStream(baos.toByteArray())));

        Assert.assertEquals("Expected a single box in the IsoFile structure", 1, singleBoxIsoFile.getBoxes().size());
        Assert.assertEquals("Expected to find a box of type " + clazz, clazz, singleBoxIsoFile.getBoxes().get(0).getClass());

        T parsedBox = (T) singleBoxIsoFile.getBoxes().get(0);


        for (String property : props.keySet()) {
            boolean found = false;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (property.equals(propertyDescriptor.getName())) {
                    found = true;
                    Assert.assertEquals("Writing and parsing changed the value", props.get(property).toString(), (Object) propertyDescriptor.getReadMethod().invoke(parsedBox).toString());                }
            }
            if (!found) {
                Assert.fail("Could not find any property descriptor for " + property);
            }
        }

        Assert.assertEquals("Writing and parsing should not change the box size.", box.getSize(), parsedBox.getSize());


        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (!props.containsKey(propertyDescriptor.getName())) {
                if (!skipList.contains(propertyDescriptor.getName())) {
                    System.out.println("No value given for property " + propertyDescriptor.getName());
                }
            }
        }


    }
}
