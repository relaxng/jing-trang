package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public abstract class AttributeUse extends Located {
  public AttributeUse(SourceLocation location) {
    super(location);
  }

  public abstract Object accept(AttributeUseVisitor visitor);
}
