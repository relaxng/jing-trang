package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class SimpleTypeRef extends SimpleType {
  private final String name;

  public SimpleTypeRef(SourceLocation location, String name) {
    super(location);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Object accept(SimpleTypeVisitor visitor) {
    return visitor.visitRef(this);
  }
}
