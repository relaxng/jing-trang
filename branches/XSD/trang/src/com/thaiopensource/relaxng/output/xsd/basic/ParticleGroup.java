package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;
import java.util.Collections;

public abstract class ParticleGroup extends Particle {
  private final List children;

  public ParticleGroup(SourceLocation location, Annotation annotation, List children) {
    super(location, annotation);
    this.children = Collections.unmodifiableList(children);
  }

  public List getChildren() {
    return children;
  }

  public int hashCode() {
    return getChildren().hashCode();
  }
}
