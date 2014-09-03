package com.thaiopensource.relaxng.edit;

import java.util.ArrayList;
import java.util.List;

public abstract class CompositePattern extends Pattern {
  private final List<Pattern> children = new ArrayList<Pattern>();
  public List<Pattern> getChildren() {
    return children;
  }
  public void childrenAccept(PatternVisitor<?> visitor) {
    for (Pattern p : children)
      p.accept(visitor);
  }
}
