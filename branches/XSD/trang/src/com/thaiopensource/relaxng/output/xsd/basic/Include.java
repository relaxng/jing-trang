package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class Include extends TopLevel {
  private final Schema includedSchema;

  Include(SourceLocation location, Schema parentSchema, Schema includedSchema) {
    super(location, parentSchema);
    this.includedSchema = includedSchema;
  }

  public Schema getIncludedSchema() {
    return includedSchema;
  }

  public void accept(SchemaVisitor visitor) {
    visitor.visitInclude(this);
  }
}
