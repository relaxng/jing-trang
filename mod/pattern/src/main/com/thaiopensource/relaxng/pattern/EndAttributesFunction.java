package com.thaiopensource.relaxng.pattern;

class EndAttributesFunction extends AbstractPatternFunction<Pattern> {
  private final ValidatorPatternBuilder builder;

  EndAttributesFunction(ValidatorPatternBuilder builder) {
    this.builder = builder;
  }

  public Pattern caseOther(Pattern p) {
    return p;
  }

  public Pattern caseGroup(GroupPattern p) {
    Pattern p1 = p.getOperand1();
    Pattern p2 = p.getOperand2();
    Pattern q1 = memoApply(p1);
    Pattern q2 = memoApply(p2);
    if (p1 == q1 && p2 == q2)
      return p;
    return builder.makeGroup(q1, q2);
  }

  public Pattern caseInterleave(InterleavePattern p) {
    Pattern p1 = p.getOperand1();
    Pattern p2 = p.getOperand2();
    Pattern q1 = memoApply(p1);
    Pattern q2 = memoApply(p2);
    if (p1 == q1 && p2 == q2)
      return p;
    return builder.makeInterleave(q1, q2);
  }

  public Pattern caseChoice(ChoicePattern p) {
    Pattern p1 = p.getOperand1();
    Pattern p2 = p.getOperand2();
    Pattern q1 = memoApply(p1);
    Pattern q2 = memoApply(p2);
    if (p1 == q1 && p2 == q2)
      return p;
    return builder.makeChoice(q1, q2);
  }

  public Pattern caseOneOrMore(OneOrMorePattern p) {
    Pattern p1 = p.getOperand();
    Pattern q1 = memoApply(p1);
    if (p1 == q1)
      return p;
    return builder.makeOneOrMore(q1);
  }

  public Pattern caseAfter(AfterPattern p) {
    Pattern p1 = p.getOperand1();
    Pattern q1 = memoApply(p1);
    if (p1 == q1)
      return p;
    return builder.makeAfter(q1, p.getOperand2());
  }

  public Pattern caseAttribute(AttributePattern p) {
    return builder.makeNotAllowed();
  }

  final Pattern memoApply(Pattern p) {
    return apply(builder.getPatternMemo(p)).getPattern();
  }

  PatternMemo apply(PatternMemo memo) {
    return memo.endAttributes(this);
  }

  ValidatorPatternBuilder getPatternBuilder() {
    return builder;
  }
}
