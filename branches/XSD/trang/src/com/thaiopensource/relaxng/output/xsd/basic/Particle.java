package com.thaiopensource.relaxng.output.xsd.basic;

import com.thaiopensource.relaxng.edit.SourceLocation;

public abstract class Particle extends Located {
  public Particle(SourceLocation location) {
    super(location);
  }

  public abstract Object accept(ParticleVisitor visitor);
}
