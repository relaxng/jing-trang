package com.thaiopensource.relaxng.edit;

public class EmptyPattern extends Pattern {
  public EmptyPattern() {
  }

  Object accept(PatternVisitor visitor) {
    return visitor.visitEmpty(this);
  }
}
