package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public class Facet extends Located {
  private final String name;
  private final String value;

  public Facet(SourceLocation location, String name, String value) {
    super(location);
    this.name = name;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }
}
