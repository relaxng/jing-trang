package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.util.VoidValue;

/**
 * PatternFunction to compute the name class of possible attributes.
 * Computes a NormalizedNameClass.
 */
class PossibleAttributeNamesFunction extends PossibleNamesFunction {
  public VoidValue caseAttribute(AttributePattern p) {
    add(p.getNameClass());
    return VoidValue.VOID;
  }

  public VoidValue caseGroup(GroupPattern p) {
    return caseBinary(p);
  }
}
