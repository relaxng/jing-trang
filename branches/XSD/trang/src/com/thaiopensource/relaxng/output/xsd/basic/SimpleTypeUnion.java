package com.thaiopensource.relaxng.output.xsd.basic;

import java.util.List;
import java.util.Collections;

public class SimpleTypeUnion extends SimpleType {
  private final List children;

  public SimpleTypeUnion(List children) {
    this.children = Collections.unmodifiableList(children);
  }

  public List getChildren() {
    return children;
  }

  public Object accept(SimpleTypeVisitor visitor) {
    return visitor.visitUnion(this);
  }
}
