package com.thaiopensource.relaxng.edit;

public class TextPattern extends Pattern {
  public TextPattern() {
  }

  Object accept(PatternVisitor visitor) {
    return visitor.visitText(this);
  }
}
