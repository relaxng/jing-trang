package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class AttributeGroupRef extends AttributeUse {
  private final String name;

  public AttributeGroupRef(SourceLocation location, String name) {
    super(location);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public Object accept(AttributeUseVisitor visitor) {
    return visitor.visitAttributeGroupRef(this);
  }

  public boolean equals(Object obj) {
    return obj instanceof AttributeGroupRef && ((AttributeGroupRef)obj).name.equals(name);
  }

  public int hashCode() {
    return name.hashCode();
  }
}
