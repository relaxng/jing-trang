package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class OptionalAttribute extends AttributeUse {
  private final Attribute attribute;

  public OptionalAttribute(SourceLocation location, Attribute attribute) {
    super(location);
    this.attribute = attribute;
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public Object accept(AttributeUseVisitor visitor) {
    return visitor.visitOptionalAttribute(this);
  }
}
