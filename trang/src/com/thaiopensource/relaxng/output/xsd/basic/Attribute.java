package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.output.common.Name;
import com.thaiopensource.relaxng.edit.SourceLocation;

public class Attribute extends AttributeUse implements Structure {
  private final Name name;
  private final SimpleType type;
  private final Use use;

  static public class Use {
    private final String name;
    private Use(String name) {
      this.name = name;
    }
  }

  static public final Use REQUIRED = new Use("REQUIRED");
  static public final Use OPTIONAL = new Use("OPTIONAL");
  static public final Use PROHIBITED = new Use("PROHIBITED");

  /**
   * type may be null, indicating any type
   */
  public Attribute(SourceLocation location, Name name, SimpleType type, Use use) {
    super(location);
    this.name = name;
    this.type = type;
    this.use = use;
  }

  public Name getName() {
    return name;
  }

  public SimpleType getType() {
    return type;
  }

  public Use getUse() {
    return use;
  }

  public Object accept(AttributeUseVisitor visitor) {
    return visitor.visitAttribute(this);
  }

  public Object accept(StructureVisitor visitor) {
    return visitor.visitAttribute(this);
  }

}
