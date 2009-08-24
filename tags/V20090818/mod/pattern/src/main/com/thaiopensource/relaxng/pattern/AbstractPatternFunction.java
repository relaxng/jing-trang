package com.thaiopensource.relaxng.pattern;

abstract class AbstractPatternFunction<T> implements PatternFunction<T> {
  public T caseEmpty(EmptyPattern p) {
    return caseOther(p);
  }

  public T caseNotAllowed(NotAllowedPattern p) {
    return caseOther(p);
  }

  public T caseError(ErrorPattern p) {
    return caseOther(p);
  }

  public T caseGroup(GroupPattern p) {
    return caseOther(p);
  }

  public T caseInterleave(InterleavePattern p) {
    return caseOther(p);
  }

  public T caseChoice(ChoicePattern p) {
    return caseOther(p);
  }

  public T caseOneOrMore(OneOrMorePattern p) {
    return caseOther(p);
  }

  public T caseElement(ElementPattern p) {
    return caseOther(p);
  }

  public T caseAttribute(AttributePattern p) {
    return caseOther(p);
  }

  public T caseData(DataPattern p) {
    return caseOther(p);
  }

  public T caseDataExcept(DataExceptPattern p) {
    return caseOther(p);
  }

  public T caseValue(ValuePattern p) {
    return caseOther(p);
  }

  public T caseText(TextPattern p) {
    return caseOther(p);
  }

  public T caseList(ListPattern p) {
    return caseOther(p);
  }

  public T caseAfter(AfterPattern p) {
    return caseOther(p);
  }

  public T caseRef(RefPattern p) {
    return caseOther(p);
  }

  public abstract T caseOther(Pattern p);
}
