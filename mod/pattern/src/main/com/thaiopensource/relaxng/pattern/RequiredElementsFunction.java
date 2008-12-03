package com.thaiopensource.relaxng.pattern;

/**
 * Implements a function on a pattern that returns the set of required elements.
 * The return value is a non-null Set each member of is a non-null Name. Note that
 * in the schema element foo|bar { text }, neither foo nor bar are required elements.
 */
public class RequiredElementsFunction extends RequiredElementsOrAttributesFunction {
  public Object caseElement(ElementPattern p) {
    return caseNamed(p.getNameClass());
  }

  public Object caseGroup(GroupPattern p) {
    Pattern p1 = p.getOperand1();
    if (!p1.isNullable())
      return p1.apply(this);
    return p.getOperand2().apply(this);
  }
}
