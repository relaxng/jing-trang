package com.thaiopensource.relaxng;

class NotAllowedPattern extends Pattern {
  NotAllowedPattern() {
    super(false, EMPTY_CONTENT_TYPE, NOT_ALLOWED_HASH_CODE);
  }
  Pattern residual(PatternBuilder b, Atom a) {
    return this;
  }
  boolean isNotAllowed() {
    return true;
  }
  boolean samePattern(Pattern other) {
    return other instanceof NotAllowedPattern;
  }
  void accept(PatternVisitor visitor) {
    visitor.visitNotAllowed();
  }
}
