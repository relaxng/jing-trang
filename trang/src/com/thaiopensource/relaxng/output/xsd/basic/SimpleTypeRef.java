package com.thaiopensource.relaxng.output.xsd.basic;

public class SimpleTypeRef extends SimpleType {
  private final String name;

  public SimpleTypeRef(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Object accept(SimpleTypeVisitor visitor) {
    return visitor.visitRef(this);
  }
}
