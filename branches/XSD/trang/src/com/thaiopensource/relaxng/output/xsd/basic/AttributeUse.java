package com.thaiopensource.relaxng.output.xsd.basic;

public abstract class AttributeUse {
  public abstract Object accept(AttributeUseVisitor visitor);
}
