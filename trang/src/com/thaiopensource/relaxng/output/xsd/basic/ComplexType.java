package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;
import java.util.Collections;

public abstract class ComplexType {
  private final List attributeUses;

  public ComplexType(List attributeUses) {
    this.attributeUses = Collections.unmodifiableList(attributeUses);
  }

  public List getAttributeUses() {
    return attributeUses;
  }

  public abstract Object accept(ComplexTypeVisitor visitor);
}
