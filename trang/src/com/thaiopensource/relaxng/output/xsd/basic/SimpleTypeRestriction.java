package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;
import java.util.Collections;

public class SimpleTypeRestriction extends SimpleType {
  private final String name;
  private final List facets;

  public SimpleTypeRestriction(SourceLocation location, String name, List facets) {
    super(location);
    this.name = name;
    this.facets = Collections.unmodifiableList(facets);
  }

  /**
   * Name is the name of a builtin simple type.
   * factes is a list of facets
   */

  public String getName() {
    return name;
  }

  public List getFacets() {
    return facets;
  }

  public Object accept(SimpleTypeVisitor visitor) {
    return visitor.visitRestriction(this);
  }
}
