package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.output.common.Name;
import com.thaiopensource.relaxng.edit.SourceLocation;

public class Attribute extends SingleAttributeUse implements Structure {
  private final Name name;
  private final SimpleType type;

  /**
   * type may be null, indicating any type
   */
  public Attribute(SourceLocation location, Name name, SimpleType type) {
    super(location);
    this.name = name;
    this.type = type;
  }

  public Name getName() {
    return name;
  }

  public SimpleType getType() {
    return type;
  }

  public Object accept(AttributeUseVisitor visitor) {
    return visitor.visitAttribute(this);
  }

  public Object accept(StructureVisitor visitor) {
    return visitor.visitAttribute(this);
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof Attribute))
      return false;
    Attribute other = (Attribute)obj;
    if (type == null) {
      if (other.type != null)
        return false;
    }
    else if (!type.equals(other.type))
      return false;
    return this.name.equals(other.name);
  }

  public int hashCode() {
    int hc = name.hashCode();
    if (type != null)
      hc ^= type.hashCode();
    return hc;
  }

  public boolean isOptional() {
    return false;
  }
}
