package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public abstract class Definition extends TopLevel {
  private final String name;

  public Definition(SourceLocation location, Schema parentSchema, String name) {
    super(location, parentSchema);
    this.name = name;
  }

  public String getName() {
    return name;
  }
}
