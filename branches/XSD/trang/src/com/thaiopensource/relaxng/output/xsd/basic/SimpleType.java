package com.thaiopensource.relaxng.output.xsd.basic;

public abstract class SimpleType {
  public abstract Object accept(SimpleTypeVisitor visitor);
}
