package com.googlecode.mp4parser.boxes;


import com.mp4parser.IsoFile;
import com.mp4parser.ParsableBox;
import com.mp4parser.support.AbstractContainerBox;
import com.mp4parser.tools.ByteBufferByteChannel;
import com.mp4parser.Box;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RunWith(Parameterized.class)
public abstract class BoxRoundtripTest {

    /*


    @Parameterized.Parameters
    public static Collection<Object[]> data() {


        return Collections.singletonList(
                new Object[]{new Box(),
                        new Map.Entry[]{
                                new E("prop", 1),
                                new E("prop2", 21)}
                });
    }



     */

    String dummyParent = null;
    ParsableBox parsableBoxUnderTest;
    Map<String, Object> props;


    public BoxRoundtripTest(ParsableBox parsableBoxUnderTest, Map.Entry<String, Object>... properties) {
        this.parsableBoxUnderTest = parsableBoxUnderTest;
        this.props = new HashMap<String, Object>();
        for (Map.Entry<String, Object> property : properties) {
            props.put(property.getKey(), property.getValue());
        }
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


    @Test
    public void roundtrip() throws Exception {

        BeanInfo beanInfo = Introspector.getBeanInfo(parsableBoxUnderTest.getClass());
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (String property : props.keySet()) {
            boolean found = false;
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                if (property.equals(propertyDescriptor.getName())) {
                    found = true;
                    try {
                        propertyDescriptor.getWriteMethod().invoke(parsableBoxUnderTest, props.get(property));
                    } catch (IllegalArgumentException e) {

                        System.err.println(propertyDescriptor.getWriteMethod().getName() + "(" + propertyDescriptor.getWriteMethod().getParameterTypes()[0].getSimpleName() + ");");
                        System.err.println("Called with " + props.get(property).getClass());


                        throw e;
                    }
                    // do the next assertion on string level to not trap into the long vs java.lang.Long pitfall
                    Assert.assertEquals("The symmetry between getter/setter of " + property + " is not given.", props.get(property), propertyDescriptor.getReadMethod().invoke(parsableBoxUnderTest));
                }
            }
            if (!found) {
                Assert.fail("Could not find any property descriptor for " + property);
            }
        }


        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        WritableByteChannel wbc = Channels.newChannel(baos);
        parsableBoxUnderTest.getBox(wbc);
        wbc.close();
        baos.close();

        IsoFile singleBoxIsoFile = new IsoFile(new ByteBufferByteChannel(baos.toByteArray()));

        Assert.assertEquals("Expected box and file size to be the same", parsableBoxUnderTest.getSize(), baos.size());
        Assert.assertEquals("Expected a single box in the IsoFile structure", 1, singleBoxIsoFile.getBoxes().size());
        Assert.assertEquals("Expected to find a box of different type ", parsableBoxUnderTest.getClass(), singleBoxIsoFile.getBoxes().get(0).getClass());

        Box parsedBox = singleBoxIsoFile.getBoxes().get(0);


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
                        Assert.assertEquals("Writing and parsing changed the value of " + property, props.get(property), propertyDescriptor.getReadMethod().invoke(parsedBox));
                    }
                }
            }
            if (!found) {
                Assert.fail("Could not find any property descriptor for " + property);
            }
        }

        Assert.assertEquals("Writing and parsing should not change the box size.", parsableBoxUnderTest.getSize(), parsedBox.getSize());

        boolean output = false;
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if (!props.containsKey(propertyDescriptor.getName())) {
                if (!skipList.contains(propertyDescriptor.getName())) {
                    if (!output) {
                        System.out.println("No value given for the following properties: ");
                        output = true;
                    }
                    System.out.println(String.format("new E(\"%s\", (%s)) ),", propertyDescriptor.getName(), propertyDescriptor.getPropertyType().getSimpleName()));
                }
            }
        }

    }

    class DummyContainerBox extends AbstractContainerBox {

        public DummyContainerBox(String type) {
            super(type);
        }
    }

    protected static class E implements Map.Entry<String,Object> {

        private String key;
        private Object value;

        public E(String key, Object value) {
            this.key = key;
            this.value = value;
        }

        public boolean equals(Object o) {
            if (!(o instanceof E)) {
                return false;
            }
            E other = (E) o;
            return other.key.equals(key) &&
                    (value == null ? other.value == null : other.value.equals(value));
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object newValue) {
            throw new UnsupportedOperationException();
        }

        public int hashCode() {
            return key.hashCode() ^ (value==null ? 0 : value.hashCode());
        }

        public String toString() {
            return key +"="+value.toString();
        }
    }

}
