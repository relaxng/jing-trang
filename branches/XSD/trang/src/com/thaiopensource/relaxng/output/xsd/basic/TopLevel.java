package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public abstract class TopLevel extends Located {
  private final Schema parentSchema;
  TopLevel(SourceLocation location, Schema parentSchema) {
    super(location);
    this.parentSchema = parentSchema;
  }

  public abstract void accept(SchemaVisitor visitor);

  public Schema getParentSchema() {
    return parentSchema;
  }
}
