package com.thaiopensource.relaxng.pattern;

abstract class StringPattern extends Pattern {
  StringPattern(int hc) {
    super(false, DATA_CONTENT_TYPE, hc);
  }
}
