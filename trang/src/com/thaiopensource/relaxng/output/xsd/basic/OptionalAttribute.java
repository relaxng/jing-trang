package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;
import com.thaiopensource.relaxng.output.common.Name;

public class OptionalAttribute extends SingleAttributeUse {
  private final Attribute attribute;

  public OptionalAttribute(SourceLocation location, Annotation annotation, Attribute attribute) {
    super(location, annotation);
    this.attribute = attribute;
  }

  public Attribute getAttribute() {
    return attribute;
  }

  public Object accept(AttributeUseVisitor visitor) {
    return visitor.visitOptionalAttribute(this);
  }

  public Name getName() {
    return attribute.getName();
  }

  public SimpleType getType() {
    return attribute.getType();
  }

  public boolean isOptional() {
    return true;
  }
}
