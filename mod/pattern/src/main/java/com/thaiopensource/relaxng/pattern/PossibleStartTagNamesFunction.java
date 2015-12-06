package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.util.VoidValue;

/**
 * PatternFunction to compute the name class of possible start-tags.
 * Computes a NormalizedNameClass.
 */
class PossibleStartTagNamesFunction extends PossibleNamesFunction {
  public VoidValue caseElement(ElementPattern p) {
    add(p.getNameClass());
    return VoidValue.VOID;
  }

  public VoidValue caseGroup(GroupPattern p) {
    p.getOperand1().apply(this);
    if (p.getOperand1().isNullable())
      p.getOperand2().apply(this);
    return VoidValue.VOID;
  }
}
