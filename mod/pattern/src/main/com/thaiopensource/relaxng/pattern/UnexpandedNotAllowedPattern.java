package com.thaiopensource.relaxng.pattern;

class UnexpandedNotAllowedPattern extends NotAllowedPattern {
  UnexpandedNotAllowedPattern() {
  }
  
  public boolean isNotAllowed() {
    return false;
  }
  Pattern expand(SchemaPatternBuilder b) {
    return b.makeNotAllowed();
  }
}
