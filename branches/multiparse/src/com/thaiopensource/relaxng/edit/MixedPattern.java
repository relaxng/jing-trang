package com.thaiopensource.relaxng.edit;

public class MixedPattern extends UnaryPattern {
  public MixedPattern(Pattern child) {
    super(child);
  }

  Object accept(PatternVisitor visitor) {
    return visitor.visitMixed(this);
  }
}
