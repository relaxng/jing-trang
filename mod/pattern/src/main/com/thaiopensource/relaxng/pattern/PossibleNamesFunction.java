package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.util.VoidValue;

/**
 * Common base class for PossibleAttributeNamesFunction and PossibleStartTagNamesFunction.
 * @see PossibleAttributeNamesFunction
 * @see PossibleStartTagNamesFunction
 */
abstract class PossibleNamesFunction extends AbstractPatternFunction<VoidValue> {
  private final UnionNameClassNormalizer normalizer = new UnionNameClassNormalizer();

  NormalizedNameClass applyTo(Pattern p) {
    normalizer.setNameClass(new NullNameClass());
    p.apply(this);
    return normalizer.normalize();
  }

  void add(NameClass nc) {
    normalizer.add(nc);
  }

  public VoidValue caseAfter(AfterPattern p) {
    return p.getOperand1().apply(this);
  }

  public VoidValue caseBinary(BinaryPattern p) {
    p.getOperand1().apply(this);
    p.getOperand2().apply(this);
    return VoidValue.VOID;
  }

  public VoidValue caseChoice(ChoicePattern p) {
    return caseBinary(p);
  }

  public VoidValue caseInterleave(InterleavePattern p) {
    return caseBinary(p);
  }

  public VoidValue caseOneOrMore(OneOrMorePattern p) {
    return p.getOperand().apply(this);
  }

  public VoidValue caseOther(Pattern p) {
    return VoidValue.VOID;
  }
}
