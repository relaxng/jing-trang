package com.thaiopensource.relaxng.pattern;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Implements a function on a pattern that returns the set of required attributes.
 * The return value is a non-null Set each member of is a non-null Name. Note that
 * in the schema attribute foo|bar { text }, neither foo nor bar are required attributes.
 */
public class RequiredAttributesFunction extends AbstractPatternFunction {
  public Object caseOther(Pattern p) {
    return Collections.EMPTY_SET;
  }

  public Object caseAttribute(AttributePattern p) {
    NameClass nc = p.getNameClass();
    if (!(nc instanceof SimpleNameClass))
      return Collections.EMPTY_SET;
    Set s = new HashSet();
    s.add(((SimpleNameClass)nc).getName());
    return s;
  }

  public Object caseChoice(ChoicePattern p) {
    Set s1 = (Set)p.getOperand1().apply(this);
    Set s2 = (Set)p.getOperand2().apply(this);
    if (s1.isEmpty())
      return s1;
    if (s2.isEmpty())
      return s2;
    s1.retainAll(s2);
    return s1;
  }

  public Object caseBinary(BinaryPattern p) {
    Set s1 = (Set)p.getOperand1().apply(this);
    Set s2 = (Set)p.getOperand2().apply(this);
    if (s1.isEmpty())
      return s2;
    if (s2.isEmpty())
      return s1;
    s1.addAll(s2);
    return s1;
  }

  public Object caseGroup(GroupPattern p) {
    return caseBinary(p);
  }

  public Object caseInterleave(InterleavePattern p) {
    return caseBinary(p);
  }

  public Object caseAfter(AfterPattern p) {
    return p.getOperand1().apply(this);
  }

  public Object caseOneOrMore(OneOrMorePattern p) {
    return p.getOperand().apply(this);
  }
}
