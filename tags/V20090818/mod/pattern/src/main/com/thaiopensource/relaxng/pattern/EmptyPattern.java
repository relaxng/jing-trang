package com.thaiopensource.relaxng.pattern;

class EmptyPattern extends Pattern {
  EmptyPattern() {
    super(true, EMPTY_CONTENT_TYPE, EMPTY_HASH_CODE);
  }
  boolean samePattern(Pattern other) {
    return other instanceof EmptyPattern;
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseEmpty(this);
  }
  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_empty");
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_empty");
    }
  }
}
