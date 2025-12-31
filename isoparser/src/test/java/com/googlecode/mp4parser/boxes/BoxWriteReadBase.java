package com.googlecode.mp4parser.boxes;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mp4parser.ParsableBox;
import org.mp4parser.PropertyBoxParserImpl;
import org.mp4parser.support.AbstractContainerBox;
import org.mp4parser.tools.ByteBufferByteChannel;

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

public abstract class BoxWriteReadBase<T extends ParsableBox> {

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
    String dummyParent = null;

    protected BoxWriteReadBase(String dummyParent) {
        this.dummyParent = dummyParent;
    }

    protected BoxWriteReadBase() {
    }

    public abstract Class<T> getBoxUnderTest();

    public abstract void setupProperties(Map<String, Object> addPropsHere, T box);


    protected T getInstance(Class<T> clazz) throws Exception {
        Constructor<T> constructor = clazz.getConstructor();
        return constructor.newInstance();
    }

    @Test
    void roundtrip() throws Exception {
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
                    Assertions.assertEquals(props.get(property), propertyDescriptor.getReadMethod().invoke(box),
                            "The symmetry between getter/setter of " + property + " is not given.");
                }
            }
            if (!found) {
                Assertions.fail("Could not find any property descriptor for " + property);
            }
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel wbc = Channels.newChannel(baos);
        box.getBox(wbc);
        wbc.close();
        baos.close();

        DummyContainerBox singleBoxIsoFile = new DummyContainerBox(dummyParent);
        singleBoxIsoFile.initContainer(new ByteBufferByteChannel(baos.toByteArray()), baos.size(), new PropertyBoxParserImpl());
        Assertions.assertEquals(box.getSize(), baos.size(),
                "Expected box and file size to be the same");
        Assertions.assertEquals(1, singleBoxIsoFile.getBoxes().size(),
                "Expected a single box in the IsoFile structure");
        Assertions.assertEquals(clazz, singleBoxIsoFile.getBoxes().get(0).getClass(),
                "Expected to find a box of different type ");

        T parsedBox = (T) singleBoxIsoFile.getBoxes().get(0);

        for (String property : props.keySet()) {
            boolean found = false;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (property.equals(propertyDescriptor.getName())) {
                    found = true;
                    if (props.get(property) instanceof int[]) {
                        Assertions.assertArrayEquals(
                                (int[]) props.get(property),
                                (int[]) propertyDescriptor.getReadMethod().invoke(parsedBox),
                                "Writing and parsing changed the value of " + property);
                    } else if (props.get(property) instanceof byte[]) {
                        Assertions.assertArrayEquals(
                                (byte[]) props.get(property),
                                (byte[]) propertyDescriptor.getReadMethod().invoke(parsedBox),
                                "Writing and parsing changed the value of " + property);
                    } else if (props.get(property) instanceof long[]) {
                        Assertions.assertArrayEquals(
                                (long[]) props.get(property),
                                (long[]) propertyDescriptor.getReadMethod().invoke(parsedBox),
                                "Writing and parsing changed the value of " + property);
                    } else {
                        Assertions.assertEquals(
                                props.get(property),
                                (Object) propertyDescriptor.getReadMethod().invoke(parsedBox),
                                "Writing and parsing changed the value of " + property);
                    }
                }
            }
            if (!found) {
                Assertions.fail("Could not find any property descriptor for " + property);
            }
        }

        Assertions.assertEquals(box.getSize(), parsedBox.getSize(),
                "Writing and parsing should not change the box size.");

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
