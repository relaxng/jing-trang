package com.thaiopensource.relaxng.pattern;

class NotAllowedPattern extends Pattern {
  NotAllowedPattern() {
    super(false, EMPTY_CONTENT_TYPE, NOT_ALLOWED_HASH_CODE);
  }
  boolean isNotAllowed() {
    return true;
  }
  boolean samePattern(Pattern other) {
    // needs to work for UnexpandedNotAllowedPattern
    return other.getClass() == this.getClass();
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseNotAllowed(this);
  }
}
