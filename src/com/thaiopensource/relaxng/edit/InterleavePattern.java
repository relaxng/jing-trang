package com.thaiopensource.relaxng.edit;

public class InterleavePattern extends CompositePattern {
  Object accept(PatternVisitor visitor) {
    return visitor.visitInterleave(this);
  }
}
