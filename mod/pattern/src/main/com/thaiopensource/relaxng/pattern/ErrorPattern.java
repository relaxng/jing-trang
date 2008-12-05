package com.thaiopensource.relaxng.pattern;

class ErrorPattern extends Pattern {
  ErrorPattern() {
    super(false, EMPTY_CONTENT_TYPE, ERROR_HASH_CODE);
  }
  boolean samePattern(Pattern other) {
    return other instanceof ErrorPattern;
  }
  void accept(PatternVisitor visitor) {
    visitor.visitError();
  }
  <T> T apply(PatternFunction<T> f) {
    return f.caseError(this);
  }
}
