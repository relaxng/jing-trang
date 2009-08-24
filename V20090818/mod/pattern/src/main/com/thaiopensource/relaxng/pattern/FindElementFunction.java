package com.thaiopensource.relaxng.pattern;

import com.thaiopensource.util.VoidValue;
import com.thaiopensource.xml.util.Name;

import java.util.HashSet;
import java.util.Set;

class FindElementFunction extends AbstractPatternFunction<VoidValue> {
  private final ValidatorPatternBuilder builder;
  private final Name name;
  private final Set<Pattern> processed = new HashSet<Pattern>();
  private int specificity = NameClass.SPECIFICITY_NONE;
  private Pattern pattern = null;

  static public Pattern findElement(ValidatorPatternBuilder builder, Name name, Pattern start) {
    FindElementFunction f = new FindElementFunction(builder, name);
    start.apply(f);
    if (f.pattern == null)
      return builder.makeNotAllowed();
    return f.pattern;
  }

  private FindElementFunction(ValidatorPatternBuilder builder, Name name) {
    this.builder = builder;
    this.name = name;
  }

  private boolean haveProcessed(Pattern p) {
    if (processed.contains(p))
      return true;
    processed.add(p);
    return false;
  }

  private VoidValue caseBinary(BinaryPattern p) {
    if (!haveProcessed(p)) {
      p.getOperand1().apply(this);
      p.getOperand2().apply(this);
    }
    return VoidValue.VOID;

 }

  public VoidValue caseGroup(GroupPattern p) {
    return caseBinary(p);
  }

  public VoidValue caseInterleave(InterleavePattern p) {
    return caseBinary(p);
  }

  public VoidValue caseChoice(ChoicePattern p) {
    return caseBinary(p);
  }

  public VoidValue caseOneOrMore(OneOrMorePattern p) {
    if (!haveProcessed(p))
      p.getOperand().apply(this);
    return VoidValue.VOID;
  }

  public VoidValue caseElement(ElementPattern p) {
    if (!haveProcessed(p)) {
      int s = p.getNameClass().containsSpecificity(name);
      if (s > specificity) {
        specificity = s;
        pattern = p.getContent();
      }
      else if (s == specificity && s != NameClass.SPECIFICITY_NONE)
        pattern = builder.makeChoice(pattern, p.getContent());
      p.getContent().apply(this);
    }
    return VoidValue.VOID;
  }

  public VoidValue caseOther(Pattern p) {
    return VoidValue.VOID;
  }
}
