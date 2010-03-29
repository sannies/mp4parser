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

package com.coremedia.drm.packager;

import junit.framework.TestCase;

import java.util.Random;
import java.util.Arrays;

/**
 * Tests packager's Base64 implementation
 */
public class Base64Test extends TestCase {
  public void testBase64_1() throws Exception {
    Random random = new Random(System.currentTimeMillis());
    byte[] orig = new byte[120];
    random.nextBytes(orig);
    String enc = Base64.byteArrayToBase64(orig);
    enc = "  " + enc + "  ";
    byte[] dec = Base64.decodeBase64(enc.getBytes("ascii"));
    assertTrue(Arrays.equals(orig, dec));
  }
}
