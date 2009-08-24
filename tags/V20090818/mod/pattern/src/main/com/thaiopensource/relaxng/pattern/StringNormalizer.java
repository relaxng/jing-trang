package com.thaiopensource.relaxng.pattern;

import java.util.StringTokenizer;

class StringNormalizer {
  static String normalize(String s) {
    StringBuilder buf = new StringBuilder();
    for (StringTokenizer e = new StringTokenizer(s); e.hasMoreElements();) {
      if (buf.length() > 0)
        buf.append(' ');
      buf.append((String)e.nextElement());
    }
    return buf.toString();
  }
}
