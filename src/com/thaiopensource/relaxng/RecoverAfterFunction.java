package com.thaiopensource.relaxng;

class RecoverAfterFunction extends AbstractPatternFunction {
  private final PatternBuilder builder;

  RecoverAfterFunction(PatternBuilder builder) {
    this.builder = builder;
  }

  public Pattern caseOther(Pattern p) {
    throw new RuntimeException("recover after botch");
  }

  public Pattern caseChoice(ChoicePattern p) {
    return builder.makeChoice(p.getOperand1().apply(this),
			      p.getOperand2().apply(this));

  }

  public Pattern caseAfter(AfterPattern p) {
    return p.getOperand2();
  }
}
