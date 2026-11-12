package com.thaiopensource.relaxng.pattern;


public class AfterPattern extends BinaryPattern {
  AfterPattern(Pattern p1, Pattern p2) {
    super(false,
	  combineHashCode(AFTER_HASH_CODE, p1.hashCode(), p2.hashCode()),
	  p1,
	  p2);
  }

  public boolean isNotAllowed() {
    return p1.isNotAllowed();
  }

  public <T> T apply(PatternFunction<T> f) {
    return f.caseAfter(this);
  }
}
