package com.thaiopensource.relaxng.pattern;

class UnexpandedNotAllowedPattern extends NotAllowedPattern {
  UnexpandedNotAllowedPattern() {
  }
  boolean isNotAllowed() {
    return false;
  }
  Pattern expand(SchemaPatternBuilder b) {
    return b.makeNotAllowed();
  }
}
