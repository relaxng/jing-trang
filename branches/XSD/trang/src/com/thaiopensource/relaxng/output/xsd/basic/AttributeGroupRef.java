package com.thaiopensource.relaxng.output.xsd.basic;

public class AttributeGroupRef extends AttributeUse {
  private final String name;

  public AttributeGroupRef(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Object accept(AttributeUseVisitor visitor) {
    return visitor.visitAttributeGroupRef(this);
  }
}
