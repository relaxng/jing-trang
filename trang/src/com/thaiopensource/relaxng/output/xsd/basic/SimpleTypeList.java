package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class SimpleTypeList extends SimpleType {
  private final SimpleType itemType;
  private final Occurs occurs;

  public SimpleTypeList(SourceLocation location, SimpleType itemType, Occurs occurs) {
    super(location);
    this.itemType = itemType;
    this.occurs = occurs;
  }

  public SimpleType getItemType() {
    return itemType;
  }

  public Occurs getOccurs() {
    return occurs;
  }

  public Object accept(SimpleTypeVisitor visitor) {
    return visitor.visitList(this);
  }
}
