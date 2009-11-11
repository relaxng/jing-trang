package com.thaiopensource.relaxng.pattern;

import java.util.HashSet;
import java.util.Set;

public class FeasibleTransform {
  private static class FeasiblePatternFunction extends AbstractPatternFunction<Pattern> {
    private final SchemaPatternBuilder spb;
    private final Set<ElementPattern> elementDone = new HashSet<ElementPattern>();

    FeasiblePatternFunction(SchemaPatternBuilder spb) {
      this.spb = spb;
    }

    public Pattern caseChoice(ChoicePattern p) {
      return spb.makeChoice(p.getOperand1().apply(this), p.getOperand2().apply(this));
    }

    public Pattern caseGroup(GroupPattern p) {
      return spb.makeGroup(p.getOperand1().apply(this), p.getOperand2().apply(this));
    }

    public Pattern caseInterleave(InterleavePattern p) {
      return spb.makeInterleave(p.getOperand1().apply(this), p.getOperand2().apply(this));
    }

    public Pattern caseOneOrMore(OneOrMorePattern p) {
      return spb.makeOneOrMore(p.getOperand().apply(this));
    }

    public Pattern caseElement(ElementPattern p) {
      if (!elementDone.contains(p)) {
        elementDone.add(p);
        p.setContent(p.getContent().apply(this));
      }
      return spb.makeOptional(p);
    }

    public Pattern caseOther(Pattern p) {
      return spb.makeOptional(p);
    }
  }

  public static Pattern transform(SchemaPatternBuilder spb, Pattern p) {
    return p.apply(new FeasiblePatternFunction(spb));
  }
}
