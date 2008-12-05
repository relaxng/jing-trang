package com.thaiopensource.relaxng.pattern;

interface PatternFunction<T> {
  T caseEmpty(EmptyPattern p);
  T caseNotAllowed(NotAllowedPattern p);
  T caseError(ErrorPattern p);
  T caseGroup(GroupPattern p);
  T caseInterleave(InterleavePattern p);
  T caseChoice(ChoicePattern p);
  T caseOneOrMore(OneOrMorePattern p);
  T caseElement(ElementPattern p);
  T caseAttribute(AttributePattern p);
  T caseData(DataPattern p);
  T caseDataExcept(DataExceptPattern p);
  T caseValue(ValuePattern p);
  T caseText(TextPattern p);
  T caseList(ListPattern p);
  T caseRef(RefPattern p);
  T caseAfter(AfterPattern p);
}
