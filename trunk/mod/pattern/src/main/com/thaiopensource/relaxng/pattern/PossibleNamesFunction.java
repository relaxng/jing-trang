package com.thaiopensource.relaxng.pattern;

/**
 * Common base class for PossibleAttributeNamesFunction and PossibleStartTagNamesFunction.
 * @see PossibleAttributeNamesFunction
 * @see PossibleStartTagNamesFunction
 */
abstract class PossibleNamesFunction extends AbstractPatternFunction {
  private UnionNameClassNormalizer normalizer = new UnionNameClassNormalizer();

  NormalizedNameClass applyTo(Pattern p) {
    p.apply(this);
    return normalizer.normalize();
  }

  void add(NameClass nc) {
    normalizer.add(nc);
  }

  public Object caseAfter(AfterPattern p) {
    return p.getOperand1().apply(this);
  }

  public Object caseBinary(BinaryPattern p) {
    p.getOperand1().apply(this);
    p.getOperand2().apply(this);
    return null;
  }

  public Object caseChoice(ChoicePattern p) {
    return caseBinary(p);
  }

  public Object caseInterleave(InterleavePattern p) {
    return caseBinary(p);
  }

  public Object caseOneOrMore(OneOrMorePattern p) {
    return p.getOperand().apply(this);
  }

  public Object caseOther(Pattern p) {
    return null;
  }
}
