package com.thaiopensource.relaxng.pattern;

class RecoverAfterFunction extends AbstractPatternFunction<Pattern> {
  private final ValidatorPatternBuilder builder;

  RecoverAfterFunction(ValidatorPatternBuilder builder) {
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
