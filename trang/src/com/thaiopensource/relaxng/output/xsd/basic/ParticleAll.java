package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;
import java.util.Collections;

public class ParticleAll extends Particle {
  private final List children;

  public ParticleAll(SourceLocation location, List children) {
    super(location);
    this.children = Collections.unmodifiableList(children);
  }

  public List getChildren() {
    return children;
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitAll(this);
  }
}
