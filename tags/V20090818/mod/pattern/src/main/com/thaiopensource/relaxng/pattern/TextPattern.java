package com.thaiopensource.relaxng.pattern;

class TextPattern extends Pattern {
  TextPattern() {
    super(true, MIXED_CONTENT_TYPE, TEXT_HASH_CODE);
  }

  boolean samePattern(Pattern other) {
    return other instanceof TextPattern;
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseText(this);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    switch (context) {
    case DATA_EXCEPT_CONTEXT:
      throw new RestrictionViolationException("data_except_contains_text");
    case START_CONTEXT:
      throw new RestrictionViolationException("start_contains_text");
    case LIST_CONTEXT:
      throw new RestrictionViolationException("list_contains_text");
    }
  }

}
