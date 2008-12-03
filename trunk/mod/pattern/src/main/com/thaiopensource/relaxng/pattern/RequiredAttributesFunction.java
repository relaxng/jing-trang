package com.thaiopensource.relaxng.pattern;

/**
 * Implements a function on a pattern that returns the set of required attributes.
 * The return value is a non-null Set each member of is a non-null Name. Note that
 * in the schema attribute foo|bar { text }, neither foo nor bar are required attributes.
 */
class RequiredAttributesFunction extends RequiredElementsOrAttributesFunction {
  public Object caseAttribute(AttributePattern p) {
    return caseNamed(p.getNameClass());
  }

  public Object caseGroup(GroupPattern p) {
    return union(p);
  }
}
