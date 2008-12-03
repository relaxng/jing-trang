package com.thaiopensource.relaxng.pattern;

/**
 * PatternFunction to compute the name class of possible attributes.
 * Computes a NormalizedNameClass.
 */
class PossibleAttributeNamesFunction extends PossibleNamesFunction {
  public Object caseAttribute(AttributePattern p) {
    add(p.getNameClass());
    return null;
  }

  public Object caseGroup(GroupPattern p) {
    return caseBinary(p);
  }
}
