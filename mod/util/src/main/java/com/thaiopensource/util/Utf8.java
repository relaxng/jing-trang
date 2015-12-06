package com.thaiopensource.util;

/**
 * Utility functions for working with UTF-8.
 */
public class Utf8 {
  private Utf8() { }

  static public byte[] encode(int c) {
    if (c < 0x80)
      return new byte[] { (byte)c };
    byte[] buf;
    if (c < 0x800) {
      buf = new byte[2];
      buf[0] = (byte)((c >> 6) | (0x80 | 0x40));
    }
    else if (c < 0x10000) {
      buf = new byte[3];
      buf[0] = (byte)((c >> (2 * 6)) | (0x80 | 0x40 | 0x20));
    }
    else {
      buf = new byte[4];
      buf[0] = (byte)((c >> (3 * 6)) | (0x80 | 0x40 | 0x20 | 0x10));
    }
    for (int i = buf.length - 1; i > 0; i--) {
      buf[i] = (byte)((c & 0x3F) | 0x80);
      c >>= 6;
    }
    return buf;
  }
}
