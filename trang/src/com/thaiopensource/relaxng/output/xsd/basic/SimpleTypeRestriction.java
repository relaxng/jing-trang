package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;
import java.util.Collections;

public class SimpleTypeRestriction extends SimpleType {
  private final String name;
  private final List facets;
  /**
   * Name is the name of a builtin simple type.
   * factes is a list of facets
   */
  public SimpleTypeRestriction(String name, List facets) {
    this.name = name;
    this.facets = Collections.unmodifiableList(facets);
  }

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
