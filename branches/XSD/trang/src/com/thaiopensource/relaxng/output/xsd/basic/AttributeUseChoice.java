package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;

public class AttributeUseChoice extends AttributeGroup {
  public AttributeUseChoice(SourceLocation location, List children) {
    super(location, children);
  }

  public Object accept(AttributeUseVisitor visitor) {
    return visitor.visitAttributeUseChoice(this);
  }
}
