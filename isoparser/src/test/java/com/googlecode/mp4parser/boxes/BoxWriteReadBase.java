package com.googlecode.mp4parser.boxes;


import com.coremedia.iso.PropertyBoxParserImpl;
import com.coremedia.iso.boxes.Box;
import com.googlecode.mp4parser.AbstractContainerBox;
import com.googlecode.mp4parser.MemoryDataSourceImpl;
import org.junit.Assert;
import org.junit.Test;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Constructor;

import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
            "path",
            "size",
            "offset",
            "type",
            "userType",
            "version");


    public abstract Class<T> getBoxUnderTest();

    public abstract void setupProperties(Map<String, Object> addPropsHere, T box);


    protected T getInstance(Class<T> clazz) throws Exception {
        Constructor<T> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

    @Test
    public void roundtrip() throws Exception {
        Class<T> clazz = getBoxUnderTest();
        T box = getInstance(clazz);
        BeanInfo beanInfo = Introspector.getBeanInfo(box.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        Map<String, Object> props = new HashMap<String, Object>();
        setupProperties(props, box);
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
                    Assert.assertEquals("The symmetry between getter/setter of " + property + " is not given.", props.get(property), propertyDescriptor.getReadMethod().invoke(box));
                }
            }
            if (!found) {
                Assert.fail("Could not find any property descriptor for " + property);
            }
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel wbc = Channels.newChannel(baos);
        box.getBox(wbc);
        wbc.close();
        baos.close();

        DummyContainerBox singleBoxIsoFile = new DummyContainerBox(dummyParent);
        singleBoxIsoFile.initContainer(new MemoryDataSourceImpl(baos.toByteArray()), baos.size(), new PropertyBoxParserImpl());
        Assert.assertEquals("Expected box and file size to be the same", box.getSize(), baos.size());
        Assert.assertEquals("Expected a single box in the IsoFile structure", 1, singleBoxIsoFile.getBoxes().size());
        Assert.assertEquals("Expected to find a box of different type ", clazz, singleBoxIsoFile.getBoxes().get(0).getClass());

        T parsedBox = (T) singleBoxIsoFile.getBoxes().get(0);


        for (String property : props.keySet()) {
            boolean found = false;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (property.equals(propertyDescriptor.getName())) {
                    found = true;
                    if (props.get(property) instanceof int[]) {
                        Assert.assertArrayEquals("Writing and parsing changed the value of " + property, (int[]) props.get(property), (int[]) propertyDescriptor.getReadMethod().invoke(parsedBox));
                    } else if (props.get(property) instanceof byte[]) {
                        Assert.assertArrayEquals("Writing and parsing changed the value of " + property, (byte[]) props.get(property), (byte[]) propertyDescriptor.getReadMethod().invoke(parsedBox));
                    } else if (props.get(property) instanceof long[]) {
                        Assert.assertArrayEquals("Writing and parsing changed the value of " + property, (long[]) props.get(property), (long[]) propertyDescriptor.getReadMethod().invoke(parsedBox));
                    } else {
                        Assert.assertEquals("Writing and parsing changed the value of " + property, props.get(property), (Object) propertyDescriptor.getReadMethod().invoke(parsedBox));
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
