package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.xml.util.Name;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Common functionality between RequiredAttributesFunction and RequiredElementsFunction
 */
abstract class RequiredElementsOrAttributesFunction extends AbstractPatternFunction<Set<Name>> {
  public Set<Name> caseOther(Pattern p) {
    return Collections.emptySet();
  }

  public Set<Name> caseChoice(ChoicePattern p) {
    Set<Name> s1 = p.getOperand1().apply(this);
    Set<Name> s2 = p.getOperand2().apply(this);
    if (s1.isEmpty())
      return s1;
    if (s2.isEmpty())
      return s2;
    s1.retainAll(s2);
    return s1;
  }

  protected Set<Name> caseNamed(NameClass nc) {
    if (!(nc instanceof SimpleNameClass))
      return Collections.emptySet();
    Set<Name> s = new HashSet<Name>();
    s.add(((SimpleNameClass)nc).getName());
    return s;
  }

  protected Set<Name> union(BinaryPattern p) {
    Set<Name> s1 = p.getOperand1().apply(this);
    Set<Name> s2 = p.getOperand2().apply(this);
    if (s1.isEmpty())
      return s2;
    if (s2.isEmpty())
      return s1;
    s1.addAll(s2);
    return s1;
  }

  public Set<Name> caseInterleave(InterleavePattern p) {
    return union(p);
  }

  public Set<Name> caseAfter(AfterPattern p) {
    return p.getOperand1().apply(this);
  }

  public Set<Name> caseOneOrMore(OneOrMorePattern p) {
    return p.getOperand().apply(this);
  }
}
