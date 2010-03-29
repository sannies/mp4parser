package com.coremedia.drm.iso;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import com.coremedia.iso.IsoOutputStream;
import com.coremedia.iso.IsoInputStream;

/**
 * Test symmetrie of IsoInputStream and Iso
 */
public class IsoStreamTest extends TestCase {

  public void testFixedPoint1616() throws IOException {
    final double fixedPointTest1 = 10.13;
    final double fixedPointTest2 = -10.13;


    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    IsoOutputStream ios = new IsoOutputStream(baos, false);
    ios.writeFixedPont1616(fixedPointTest1);
    ios.writeFixedPont1616(fixedPointTest2);
    ios.close();
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    IsoInputStream iis = new IsoInputStream(bais);
    assertEquals("fixedPointTest1", fixedPointTest1, iis.readFixedPoint1616(), 1d/65536);
    assertEquals("fixedPointTest2", fixedPointTest2, iis.readFixedPoint1616(), 1d/65536);
  }

  public void testFixedPoint88() throws IOException {
    final double fixedPointTest1 = 10.13;
    final double fixedPointTest2 = -10.13;


    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    IsoOutputStream ios = new IsoOutputStream(baos, false);
    ios.writeFixedPont88(fixedPointTest1);
    ios.writeFixedPont88(fixedPointTest2);
    ios.close();
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    IsoInputStream iis = new IsoInputStream(bais);
    assertEquals("fixedPointTest1", fixedPointTest1, iis.readFixedPoint88(), 1d/256);
    assertEquals("fixedPointTest2", fixedPointTest2, iis.readFixedPoint88(), 1d/256);
  }

}
