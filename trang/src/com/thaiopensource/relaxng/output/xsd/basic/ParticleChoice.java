package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;

public class ParticleChoice extends ParticleGroup {
  public ParticleChoice(SourceLocation location, Annotation annotation, List children) {
    super(location, annotation, children);
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitChoice(this);
  }

  public boolean equals(Object obj) {
    return obj instanceof ParticleChoice && ((ParticleChoice)obj).getChildren().equals(getChildren());
  }
}
