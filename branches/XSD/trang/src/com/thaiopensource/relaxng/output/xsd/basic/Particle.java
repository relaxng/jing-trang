package com.thaiopensource.relaxng.output.xsd.basic;

public abstract class Particle {
  public abstract Object accept(ParticleVisitor visitor);
}
