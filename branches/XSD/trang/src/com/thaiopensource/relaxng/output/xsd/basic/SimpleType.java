package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public abstract class SimpleType extends Located {
  public SimpleType(SourceLocation location) {
    super(location);
  }

  public abstract Object accept(SimpleTypeVisitor visitor);
}
