package com.thaiopensource.relaxng.edit;

public class ParentRefPattern extends AbstractRefPattern {
  public ParentRefPattern(String name) {
    super(name);
  }

  Object accept(PatternVisitor visitor) {
    return visitor.visitParentRef(this);
  }
}
