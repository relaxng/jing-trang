package com.thaiopensource.relaxng.edit;

public class ChoicePattern extends CompositePattern {
  Object accept(PatternVisitor visitor) {
    return visitor.visitChoice(this);
  }
}
