package com.thaiopensource.relaxng;

class EndTagDerivFunction extends AbstractPatternFunction {
  private final PatternBuilder builder;

  EndTagDerivFunction(PatternBuilder builder) {
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

  final private Pattern memoApply(Pattern p) {
    return apply(builder.getPatternMemo(p)).getPattern();
  }

  PatternMemo apply(PatternMemo memo) {
    return memo.endTagDeriv(this);
  }
}
