package com.thaiopensource.relaxng;

import org.relaxng.datatype.Datatype;

class ChoicePattern extends BinaryPattern {
  ChoicePattern(Pattern p1, Pattern p2) {
    super(p1.isNullable() || p2.isNullable(),
	  combineHashCode(CHOICE_HASH_CODE, p1.hashCode(), p2.hashCode()),
	  p1,
	  p2);
  }
  Pattern expand(PatternBuilder b) {
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

  Pattern combineAfter(PatternBuilder b, AfterPattern p) {
    Pattern tem = p1.combineAfter(b, p);
    if (tem != null) {
      if (tem == p1)
        return this;
      return b.makeChoice(tem, p2);
    }
    tem = p2.combineAfter(b, p);
    if (tem != null) {
      if (tem == p2)
        return this;
      return b.makeChoice(p1, tem);
    }
    return null;
  }

  void accept(PatternVisitor visitor) {
    visitor.visitChoice(p1, p2);
  }

  Object apply(PatternFunction f) {
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

