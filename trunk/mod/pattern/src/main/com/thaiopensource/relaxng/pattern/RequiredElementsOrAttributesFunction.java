package com.thaiopensource.relaxng.pattern;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

/**
 * Common functionality between RequiredAttributesFunction and RequiredElementsFunction
 */
abstract class RequiredElementsOrAttributesFunction extends AbstractPatternFunction {
  public Object caseOther(Pattern p) {
    return Collections.EMPTY_SET;
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

  protected Object caseNamed(NameClass nc) {
    if (!(nc instanceof SimpleNameClass))
      return Collections.EMPTY_SET;
    Set s = new HashSet();
    s.add(((SimpleNameClass)nc).getName());
    return s;
  }

  protected Object union(BinaryPattern p) {
    Set s1 = (Set)p.getOperand1().apply(this);
    Set s2 = (Set)p.getOperand2().apply(this);
    if (s1.isEmpty())
      return s2;
    if (s2.isEmpty())
      return s1;
    s1.addAll(s2);
    return s1;
  }

  public Object caseInterleave(InterleavePattern p) {
    return union(p);
  }

  public Object caseAfter(AfterPattern p) {
    return p.getOperand1().apply(this);
  }

  public Object caseOneOrMore(OneOrMorePattern p) {
    return p.getOperand().apply(this);
  }
}
