package com.thaiopensource.relaxng.pattern;

class ChoicePattern extends BinaryPattern {
  ChoicePattern(Pattern p1, Pattern p2) {
    super(p1.isNullable() || p2.isNullable(),
	  combineHashCode(CHOICE_HASH_CODE, p1.hashCode(), p2.hashCode()),
	  p1,
	  p2);
  }
  Pattern expand(SchemaPatternBuilder b) {
    Pattern ep1 = p1.expand(b);
    Pattern ep2 = p2.expand(b);
    if (ep1 != p1 || ep2 != p2)
      return b.makeChoice(ep1, ep2);
    else
      return this;
  }

  boolean containsChoice(Pattern p) {
    return p1.containsChoice(p) || p2.containsChoice(p);
  }

  <T> T apply(PatternFunction<T> f) {
    return f.caseChoice(this);
  }

  void checkRestrictions(int context, DuplicateAttributeDetector dad, Alphabet alpha)
    throws RestrictionViolationException {
    if (dad != null)
      dad.startChoice();
    p1.checkRestrictions(context, dad, alpha);
    if (dad != null)
      dad.alternative();
    p2.checkRestrictions(context, dad, alpha);
    if (dad != null)
      dad.endChoice();
  }

}

