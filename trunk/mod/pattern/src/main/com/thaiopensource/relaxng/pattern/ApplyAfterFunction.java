package com.thaiopensource.relaxng.pattern;

abstract class ApplyAfterFunction extends AbstractPatternFunction<Pattern> {
  private final ValidatorPatternBuilder builder;

  ApplyAfterFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  public Pattern caseAfter(AfterPattern p) {
    return builder.makeAfter(p.getOperand1(), apply(p.getOperand2()));
  }

  public Pattern caseChoice(ChoicePattern p) {
    return builder.makeChoice(p.getOperand1().apply(this),
                              p.getOperand2().apply(this));
  }

  public Pattern caseNotAllowed(NotAllowedPattern p) {
    return p;
  }

  public Pattern caseOther(Pattern p) {
    throw new RuntimeException("apply after botch");
  }

  abstract Pattern apply(Pattern p);
}