package com.coremedia.iso;

import java.io.UnsupportedEncodingException;

/**
 * Converts <code>byte[]</code> -> <code>String</code> and vice versa.
 */
public final class Ascii {
  public static byte[] convert(String s) {
    try {
      if (s != null) {
        return s.getBytes("us-ascii");
      } else {
        return null;
      }
    } catch (UnsupportedEncodingException e) {
      throw new Error(e);
    }
  }

  public static String convert(byte[] b) {
    try {
      if (b != null) {
        return new String(b, "us-ascii");
      } else {
        return null;
      }
    } catch (UnsupportedEncodingException e) {
      throw new Error(e);
    }
  }
}