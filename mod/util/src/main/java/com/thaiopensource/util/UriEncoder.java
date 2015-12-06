package com.thaiopensource.util;

/**
 *  Selectively percent-encodes characters in a URI.
 */
public class UriEncoder {
  /**
   * Flag to include U+0000 - U+001F.
   */
  static private final int C0_CONTROL = 0x01;
  /**
   * Flag to include U+0020
   */
  static private final int SPACE = 0x02;
  /**
   * Flag to include '<', '>', '"'
   */
  static private final int DELIM = 0x04;
  /**
   * Flag to include '{', '}', '|', '\\', '^', U+007E
   */
  static private final int UNWISE = 0x08;
  /**
   * Flag to include U+007F
   */
  static private final int DELETE = 0x10;
  /**
   * Flag to include U+0080 - U+009F
   */
  static private final int C1_CONTROL = 0x20;
  /**
   * Flag to include any non-ASCII character with category Zs, Zl, and Zp
   */
  static private final int NON_ASCII_SEPARATOR = 0x40;
  /**
   * Flag to include any other character with code-point >= U+0080
   */
  static private final int OTHER_NON_ASCII = 0x80;

  static private final int ASCII_CONTROL = C0_CONTROL|DELETE;
  static private final int CONTROL = ASCII_CONTROL|C1_CONTROL;
  static private final int SEPARATOR = NON_ASCII_SEPARATOR|SPACE;
  static private final int ASCII_GRAPHIC_FORBIDDEN = DELIM|UNWISE;
  static private final int ASCII_PRINTABLE_FORBIDDEN = ASCII_GRAPHIC_FORBIDDEN|SPACE;
  static private final int ASCII_FORBIDDEN = ASCII_CONTROL|ASCII_PRINTABLE_FORBIDDEN;
  static private final int NON_ASCII = C1_CONTROL|NON_ASCII_SEPARATOR|OTHER_NON_ASCII;
  static private final int JAVA_URI_FORBIDDEN = CONTROL|SEPARATOR|ASCII_PRINTABLE_FORBIDDEN;
  static private final int URI_FORBIDDEN = ASCII_FORBIDDEN|NON_ASCII;

  static public String encode(String s) {
    return encode(s, JAVA_URI_FORBIDDEN);
  }

  static public String encodeAsAscii(String s) {
    return encode(s, URI_FORBIDDEN);
  }

  static private String encode(String s, int flags) {
    StringBuffer encoded = null;
    final int len = s.length();
    for (int i = 0; i < len; i++) {
      char c = s.charAt(i);
      boolean mustEncode;
      switch (c) {
      case '<':
      case '>':
      case '"':
        mustEncode = ((flags & DELIM) != 0);
        break;
      case '{':
      case '}':
      case '|':
      case '\\':
      case '^':
      case '`':
        mustEncode = ((flags & UNWISE) != 0);
        break;
      case 0x20:
        mustEncode = ((flags & SPACE) != 0);
        break;
      case 0x7F:
        mustEncode = ((flags & DELETE) != 0);
        break;
      default:
        if (c < 0x20)
          mustEncode = ((flags & C0_CONTROL) != 0);
        else if (c < 0x80)
          mustEncode = false;
        else {
          switch (flags & NON_ASCII) {
          case NON_ASCII:
            // all non-ASCII chars need to be escaped
            mustEncode = true;
            break;
          case 0:
            // no non-ASCII chars need to be escaped
            mustEncode = false;
            break;
          default:
            if (Character.isISOControl(c))
              mustEncode = ((flags & C1_CONTROL) != 0);
            else if (Character.isSpaceChar(c))
              mustEncode = ((flags & NON_ASCII_SEPARATOR) != 0);
            else
              mustEncode = ((flags & OTHER_NON_ASCII) != 0);
            break;
          }
        }
      }
      if (mustEncode) {
        if (encoded == null)
          encoded = new StringBuffer(s.substring(0, i));
        int codePoint;
        if (Utf16.isSurrogate1(c)
            && i + 1 < len
            && Utf16.isSurrogate2(s.charAt(i + 1)))
          codePoint = Utf16.scalarValue(c, s.charAt(++i));
        else
          codePoint = c;
        encoded.append(percentEncode(Utf8.encode(codePoint)));
      }
      else if (encoded != null)
        encoded.append(c);
    }
    if (encoded != null)
      return encoded.toString();
    return s;
  }

  static private final String hexDigits = "0123456789ABCDEF";

  static char[] percentEncode(byte[] bytes) {
    char[] buf = new char[bytes.length * 3];
    int j = 0;
    for (int i = 0; i < bytes.length; i++) {
      int b = bytes[i];
      buf[j++] = '%';
      buf[j++] = hexDigits.charAt((b >> 4) & 0xF);
      buf[j++] = hexDigits.charAt(b & 0xF);
    }
    return buf;
  }
}
