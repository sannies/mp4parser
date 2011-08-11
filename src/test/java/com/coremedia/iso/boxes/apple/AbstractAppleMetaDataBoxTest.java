package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoOutputStream;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class AbstractAppleMetaDataBoxTest {
    @Test
    public void testSetValue_9() throws IOException {
        class MyConcreteAppleMetaDataBox extends AbstractAppleMetaDataBox {

            public MyConcreteAppleMetaDataBox() {
                super("test");
                this.appleDataBox = new AppleDataBox();
                this.appleDataBox.setFlags(21);
                this.appleDataBox.setContent(new byte[4]);
            }

        }

        MyConcreteAppleMetaDataBox myConcreteAppleMetaDataBox = new MyConcreteAppleMetaDataBox();
        myConcreteAppleMetaDataBox.setValue("9");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        IsoOutputStream isoOutputStream = new IsoOutputStream(baos);
        myConcreteAppleMetaDataBox.getContent(isoOutputStream);
        Assert.assertEquals("9", myConcreteAppleMetaDataBox.getValue());

    }
}
