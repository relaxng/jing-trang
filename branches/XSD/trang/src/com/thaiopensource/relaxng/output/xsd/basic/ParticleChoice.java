package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

import java.util.List;

public class ParticleChoice extends ParticleGroup {
  public ParticleChoice(SourceLocation location, List children) {
    super(location, children);
  }

  public Object accept(ParticleVisitor visitor) {
    return visitor.visitChoice(this);
  }
}
