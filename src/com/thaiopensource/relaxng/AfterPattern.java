package com.thaiopensource.relaxng;

class AfterPattern extends BinaryPattern {
  AfterPattern(Pattern p1, Pattern p2) {
    super(false,
	  combineHashCode(AFTER_HASH_CODE, p1.hashCode(), p2.hashCode()),
	  p1,
	  p2);
  }

  Pattern combineAfter(PatternBuilder b, AfterPattern p) {
    if (p == this)
      return this;
    if (p1 == p.p1)
      return b.makeAfter(p1, b.makeChoice(p.p2, p2));
    if (p2 == p.p2)
      return b.makeAfter(b.makeChoice(p1, p.p1), p2);
    return null;
  }

  Object apply(PatternFunction f) {
    return f.caseAfter(this);
  }
  void accept(PatternVisitor visitor) {
    // XXX visitor.visitAfter(p1, p2);
  }
}
