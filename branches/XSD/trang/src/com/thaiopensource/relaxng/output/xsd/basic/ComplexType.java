package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;
import java.util.Collections;

public abstract class ComplexType {
  private final AttributeUse attributeUses;

  public ComplexType(AttributeUse attributeUses) {
    this.attributeUses = attributeUses;
  }

  public AttributeUse getAttributeUses() {
    return attributeUses;
  }

  public abstract Object accept(ComplexTypeVisitor visitor);
}
