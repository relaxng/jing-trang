package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;

public class ComplexTypeSimpleContent extends ComplexType {
  private final SimpleType simpleType;

  public ComplexTypeSimpleContent(List attributeUses, SimpleType simpleType) {
    super(attributeUses);
    this.simpleType = simpleType;
  }

  public SimpleType getSimpleType() {
    return simpleType;
  }

  public Object accept(ComplexTypeVisitor visitor) {
    return visitor.visitSimpleContent(this);
  }
}
