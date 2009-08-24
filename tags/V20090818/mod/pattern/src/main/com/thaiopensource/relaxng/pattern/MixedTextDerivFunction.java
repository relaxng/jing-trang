package com.thaiopensource.relaxng.pattern;

class MixedTextDerivFunction extends EndAttributesFunction {

  MixedTextDerivFunction(ValidatorPatternBuilder builder) {
    super(builder);
  }

  public Pattern caseText(TextPattern p) {
    return p;
  }

  public Pattern caseGroup(GroupPattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    final Pattern q1 = memoApply(p1);
    Pattern tem = (q1 == p1) ? p : getPatternBuilder().makeGroup(q1, p2);
    if (!p1.isNullable())
      return tem;
    return getPatternBuilder().makeChoice(tem, memoApply(p2));
  }

  public Pattern caseInterleave(InterleavePattern p) {
    final Pattern p1 = p.getOperand1();
    final Pattern p2 = p.getOperand2();
    final Pattern q1 = memoApply(p1);
    final Pattern q2 = memoApply(p2);
    final Pattern i1 = (q1 == p1) ? p : getPatternBuilder().makeInterleave(q1, p2);
    final Pattern i2 = (q2 == p2) ? p : getPatternBuilder().makeInterleave(p1, q2);
    return getPatternBuilder().makeChoice(i1, i2);
  }

  public Pattern caseOneOrMore(OneOrMorePattern p) {
    return getPatternBuilder().makeGroup(memoApply(p.getOperand()),
					 getPatternBuilder().makeOptional(p));
  }

  public Pattern caseOther(Pattern p) {
    return getPatternBuilder().makeNotAllowed();
  }

  PatternMemo apply(PatternMemo memo) {
    return memo.mixedTextDeriv(this);
  }
}
