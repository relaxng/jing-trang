package com.thaiopensource.relaxng;

class StartTagOpenRecoverDerivFunction extends StartTagOpenDerivFunction {
  StartTagOpenRecoverDerivFunction(Name name, PatternBuilder builder) {
    super(name, builder);
  }

  public Object caseGroup(GroupPattern p) {
    Pattern tem = (Pattern)super.caseGroup(p);
    if (p.getOperand1().isNullable())
      return tem;
    return getPatternBuilder().makeChoice(tem, memoApply(p.getOperand2()), true);
 }

  PatternMemo apply(PatternMemo memo) {
    return memo.startTagOpenRecoverDeriv(this);
  }
}
