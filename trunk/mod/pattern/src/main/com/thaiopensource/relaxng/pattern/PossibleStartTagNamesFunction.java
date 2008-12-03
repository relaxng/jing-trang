package com.thaiopensource.relaxng.pattern;

/**
 * PatternFunction to compute the name class of possible start-tags.
 * Computes a NormalizedNameClass.
 */
class PossibleStartTagNamesFunction extends PossibleNamesFunction {
  public Object caseElement(ElementPattern p) {
    add(p.getNameClass());
    return null;
  }

  public Object caseGroup(GroupPattern p) {
    p.getOperand1().apply(this);
    if (p.getOperand1().isNullable())
      p.getOperand2().apply(this);
    return null;
  }
}
