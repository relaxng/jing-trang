package com.thaiopensource.relaxng.pattern;

class EndTagDerivFunction extends AbstractPatternFunction<Pattern> {
  private final ValidatorPatternBuilder builder;

  EndTagDerivFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  public Pattern caseOther(Pattern p) {
    return builder.makeNotAllowed();
  }

  public Pattern caseChoice(ChoicePattern p) {
    return builder.makeChoice(memoApply(p.getOperand1()),
			      memoApply(p.getOperand2()));
  }

  public Pattern caseAfter(AfterPattern p) {
    if (p.getOperand1().isNullable())
      return p.getOperand2();
    else
      return builder.makeNotAllowed();
  }

  private Pattern memoApply(Pattern p) {
    return apply(builder.getPatternMemo(p)).getPattern();
  }

  private PatternMemo apply(PatternMemo memo) {
    return memo.endTagDeriv(this);
  }
}
