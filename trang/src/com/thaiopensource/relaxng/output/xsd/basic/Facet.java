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

  public boolean equals(Object obj) {
    if (!(obj instanceof Facet))
      return false;
    Facet other = (Facet)obj;
    return this.name.equals(other.name) && this.value.equals(other.value);
  }

  public int hashCode() {
    return name.hashCode() ^ value.hashCode();
  }
}
