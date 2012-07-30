package com.googlecode.mp4parser.boxes;


import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.AbstractContainerBox;
import org.junit.Assert;
import org.junit.Test;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;
import java.nio.channels.Channels;
import java.util.*;

public abstract class BoxWriteReadBase<T extends Box> {

    String dummyParent = null;

    protected BoxWriteReadBase(String dummyParent) {
        this.dummyParent = dummyParent;
    }

    protected BoxWriteReadBase() {
    }

    private static final Collection<String> skipList = Arrays.asList(
            "class",
            "flags",
            "isoFile",
            "parent",
            "parsed",
            "size",
            "type",
            "userType",
            "numOfBytesToFirstChild",
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
        Map<String, Object> props = new HashMap<String, Object>();
        setupProperties(props);
        for (String property : props.keySet()) {
            boolean found = false;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (property.equals(propertyDescriptor.getName())) {
                    found = true;
                    try {
                        propertyDescriptor.getWriteMethod().invoke(box, props.get(property));
                    } catch (IllegalArgumentException e) {

                        System.err.println(propertyDescriptor.getWriteMethod().getName() + "(" + propertyDescriptor.getWriteMethod().getParameterTypes()[0].getSimpleName() + ");");
                        System.err.println("Called with " + props.get(property).getClass());


                        throw e;
                    }
                    // do the next assertion on string level to not trap into the long vs java.lang.Long pitfall
                    Assert.assertEquals("The symmetry between getter/setter is not given.", props.get(property).toString(), propertyDescriptor.getReadMethod().invoke(box).toString());
                }
            }
            if (!found) {
                Assert.fail("Could not find any property descriptor for " + property);
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        box.getBox(Channels.newChannel(baos));
        Assert.assertEquals(box.getSize(), baos.size());
        IsoFile singleBoxIsoFile = new IsoFile(Channels.newChannel(new ByteArrayInputStream(baos.toByteArray()))) {
            @Override
            public String getType() {
                return dummyParent;
            }
        };

        Assert.assertEquals("Expected a single box in the IsoFile structure", 1, singleBoxIsoFile.getBoxes().size());
        Assert.assertEquals("Expected to find a box of type " + clazz, clazz, singleBoxIsoFile.getBoxes().get(0).getClass());

        T parsedBox = (T) singleBoxIsoFile.getBoxes().get(0);


        for (String property : props.keySet()) {
            boolean found = false;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (property.equals(propertyDescriptor.getName())) {
                    found = true;
                    if (props.get(property) instanceof int[]) {
                        Assert.assertArrayEquals("Writing and parsing changed the value", (int[]) props.get(property), (int[]) propertyDescriptor.getReadMethod().invoke(parsedBox));
                    } else if (props.get(property) instanceof long[]) {
                        Assert.assertArrayEquals("Writing and parsing changed the value", (long[]) props.get(property), (long[]) propertyDescriptor.getReadMethod().invoke(parsedBox));
                    } else if (props.get(property) instanceof List) {
                        Assert.assertEquals("Writing and parsing changed the value", (List) props.get(property), (List) propertyDescriptor.getReadMethod().invoke(parsedBox));
                    } else {
                        Assert.assertEquals("Writing and parsing changed the value", props.get(property).toString(), (Object) propertyDescriptor.getReadMethod().invoke(parsedBox).toString());
                    }
                }
            }
            if (!found) {
                Assert.fail("Could not find any property descriptor for " + property);
            }
        }

        Assert.assertEquals("Writing and parsing should not change the box size.", box.getSize(), parsedBox.getSize());

        boolean output = false;
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (!props.containsKey(propertyDescriptor.getName())) {
                if (!skipList.contains(propertyDescriptor.getName())) {
                    if (!output) {
                        System.out.println("No value given for the following properties: ");
                        output = true;
                    }
                    System.out.println(String.format("addPropsHere.put(\"%s\", (%s) );", propertyDescriptor.getName(), propertyDescriptor.getPropertyType().getSimpleName()));
                }
            }
        }
    }

    class DummyContainerBox extends AbstractContainerBox {

        public DummyContainerBox(String type) {
            super(type);
        }
    }

}
