package com.thaiopensource.relaxng;

abstract class AbstractPatternFunction implements PatternFunction {
  public Pattern caseEmpty(EmptyPattern p) {
    return caseOther(p);
  }

  public Pattern caseNotAllowed(NotAllowedPattern p) {
    return caseOther(p);
  }

  public Pattern caseError(ErrorPattern p) {
    return caseOther(p);
  }

  public Pattern caseGroup(GroupPattern p) {
    return caseOther(p);
  }

  public Pattern caseInterleave(InterleavePattern p) {
    return caseOther(p);
  }

  public Pattern caseChoice(ChoicePattern p) {
    return caseOther(p);
  }

  public Pattern caseOneOrMore(OneOrMorePattern p) {
    return caseOther(p);
  }

  public Pattern caseElement(ElementPattern p) {
    return caseOther(p);
  }

  public Pattern caseAttribute(AttributePattern p) {
    return caseOther(p);
  }

  public Pattern caseData(DataPattern p) {
    return caseOther(p);
  }

  public Pattern caseDataExcept(DataExceptPattern p) {
    return caseOther(p);
  }

  public Pattern caseValue(ValuePattern p) {
    return caseOther(p);
  }

  public Pattern caseText(TextPattern p) {
    return caseOther(p);
  }

  public Pattern caseList(ListPattern p) {
    return caseOther(p);
  }

  public abstract Pattern caseOther(Pattern p);
}
