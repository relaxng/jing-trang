package com.thaiopensource.relaxng.edit;

public class OneOrMorePattern extends UnaryPattern {
  public OneOrMorePattern(Pattern child) {
    super(child);
  }

  Object accept(PatternVisitor visitor) {
    return visitor.visitOneOrMore(this);
  }
}
