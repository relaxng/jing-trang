package com.thaiopensource.relaxng;

interface PatternFunction {
  Pattern caseEmpty(EmptyPattern p);
  Pattern caseNotAllowed(NotAllowedPattern p);
  Pattern caseError(ErrorPattern p);
  Pattern caseGroup(GroupPattern p);
  Pattern caseInterleave(InterleavePattern p);
  Pattern caseChoice(ChoicePattern p);
  Pattern caseOneOrMore(OneOrMorePattern p);
  Pattern caseElement(ElementPattern p);
  Pattern caseAttribute(AttributePattern p);
  Pattern caseData(DataPattern p);
  Pattern caseDataExcept(DataExceptPattern p);
  Pattern caseValue(ValuePattern p);
  Pattern caseText(TextPattern p);
  Pattern caseList(ListPattern p);
  Pattern caseAfter(AfterPattern p);
}
