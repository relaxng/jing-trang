package com.thaiopensource.relaxng;

class UnexpandedNotAllowedPattern extends NotAllowedPattern {
  UnexpandedNotAllowedPattern() {
  }
  boolean isNotAllowed() {
    return false;
  }
  Pattern expand(PatternBuilder b) {
    return b.makeNotAllowed();
  }
}
